package com.group2.claims_service.service;

import com.group2.claims_service.dto.*;
import com.group2.claims_service.entity.*;
import com.group2.claims_service.exception.ClaimNotFoundException;
import com.group2.claims_service.repository.*;
import com.group2.claims_service.service.impl.ClaimServiceImpl;
import com.group2.claims_service.service.EmailService;
import com.group2.claims_service.client.PolicyClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaimServiceTest {

    @InjectMocks
    private ClaimServiceImpl claimService;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimDocumentRepository documentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PolicyClient policyClient;

    // ==================== initiateClaim ====================

    @Test
    @DisplayName("Should initiate a claim successfully when amount is within coverage")
    void testInitiateClaim() {
        ClaimRequestDTO request = new ClaimRequestDTO();
        request.setPolicyId(1L);
        request.setUserId(1L);
        request.setClaimAmount(5000.0);
        request.setDescription("Accident");

        UserPolicyResponseDTO policy = new UserPolicyResponseDTO();
        policy.setCoverageAmount(10000.0);
        when(policyClient.getUserPolicyById(eq(1L), anyString())).thenReturn(policy);

        Claim claim = new Claim();
        claim.setId(1L);
        claim.setPolicyId(1L);
        claim.setUserId(1L);
        claim.setClaimAmount(5000.0);
        claim.setDescription("Accident");
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        ClaimResponseDTO result = claimService.initiateClaim(request);

        assertNotNull(result);
        assertEquals(1L, result.getClaimId());
        assertEquals("SUBMITTED", result.getStatus());
        assertEquals(5000.0, result.getClaimAmount());
        assertEquals("Accident", result.getDescription());
        assertEquals("Claim submitted successfully", result.getMessage());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), (Object) any());
    }

    @Test
    @DisplayName("Should throw RuntimeException when claim amount exceeds coverage")
    void testInitiateClaim_ClaimAmountExceedsCoverage() {
        ClaimRequestDTO request = new ClaimRequestDTO();
        request.setPolicyId(1L);
        request.setUserId(1L);
        request.setClaimAmount(20000.0);
        request.setDescription("Expensive Claim");

        UserPolicyResponseDTO policy = new UserPolicyResponseDTO();
        policy.setCoverageAmount(10000.0);
        when(policyClient.getUserPolicyById(eq(1L), anyString())).thenReturn(policy);

        assertThrows(RuntimeException.class, () -> claimService.initiateClaim(request));
        verify(claimRepository, never()).save(any());
    }



    @Test
    @DisplayName("Should send email to user when user is found after claim initiation")
    void testInitiateClaim_WithEmailSentToUser() {
        ClaimRequestDTO request = new ClaimRequestDTO();
        request.setPolicyId(1L);
        request.setUserId(1L);
        request.setClaimAmount(3000.0);
        request.setDescription("Car Damage");

        UserPolicyResponseDTO policy = new UserPolicyResponseDTO();
        policy.setCoverageAmount(10000.0);
        when(policyClient.getUserPolicyById(eq(1L), anyString())).thenReturn(policy);

        Claim claim = new Claim();
        claim.setId(3L);
        claim.setPolicyId(1L);
        claim.setUserId(1L);
        claim.setClaimAmount(3000.0);
        claim.setDescription("Car Damage");
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ClaimResponseDTO result = claimService.initiateClaim(request);

        assertNotNull(result);
        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ==================== uploadDocument ====================

    @Test
    @DisplayName("Should upload PDF document successfully")
    void testUploadDocument_PdfContentType() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getBytes()).thenReturn(new byte[10]);

        Claim claim = new Claim();
        claim.setId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        String result = claimService.uploadDocument(1L, file);
        assertEquals("Document uploaded Successfully", result);
        verify(documentRepository, times(1)).save(any(ClaimDocument.class));
    }




    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid file format")
    void testUploadDocument_InvalidFileFormat() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getOriginalFilename()).thenReturn("file.txt");

        Claim claim = new Claim();
        claim.setId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThrows(IllegalArgumentException.class, () -> claimService.uploadDocument(1L, file));
    }

    @Test
    @DisplayName("Should throw ClaimNotFoundException when uploading to non-existent claim")
    void testUploadDocument_ClaimNotFound() {
        MultipartFile file = mock(MultipartFile.class);
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClaimNotFoundException.class, () -> claimService.uploadDocument(99L, file));
    }

    @Test
    @DisplayName("Should throw RuntimeException when IOException occurs reading file bytes")
    void testUploadDocument_IOExceptionOnBytes() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("doc.pdf");
        when(file.getBytes()).thenThrow(new IOException("Disk full"));

        Claim claim = new Claim();
        claim.setId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        assertThrows(RuntimeException.class, () -> claimService.uploadDocument(1L, file));
    }

    // ==================== getClaimDocument ====================

    @Test
    @DisplayName("Should return claim document successfully")
    void testGetClaimDocument_Success() {
        ClaimDocument doc = new ClaimDocument();
        doc.setFileData(new byte[]{1, 2, 3});
        when(documentRepository.findFirstByClaimIdOrderByIdDesc(1L)).thenReturn(Optional.of(doc));

        ClaimDocument result = claimService.getClaimDocument(1L);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should throw ClaimNotFoundException when document not found")
    void testGetClaimDocument_NotFound() {
        when(documentRepository.findFirstByClaimIdOrderByIdDesc(99L)).thenReturn(Optional.empty());
        assertThrows(ClaimNotFoundException.class, () -> claimService.getClaimDocument(99L));
    }

    // ==================== getClaimStatus ====================

    @Test
    @DisplayName("Should return claim status successfully")
    void testGetClaimStatus() {
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setClaimStatus(ClaimStatus.APPROVED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        ClaimResponseDTO result = claimService.getClaimStatus(1L);
        assertEquals(1L, result.getClaimId());
        assertEquals("APPROVED", result.getStatus());
        assertEquals("Claim Status fetched Successfully", result.getMessage());
    }

    @Test
    @DisplayName("Should throw ClaimNotFoundException when claim not found for status")
    void testGetClaimStatus_NotFound() {
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ClaimNotFoundException.class, () -> claimService.getClaimStatus(99L));
    }

    // ==================== getClaimById ====================

    @Test
    @DisplayName("Should return full claim details by ID")
    void testGetClaimById_Success() {
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setPolicyId(2L);
        claim.setUserId(3L);
        claim.setClaimAmount(8000.0);
        claim.setDescription("Medical Treatment");
        claim.setClaimStatus(ClaimStatus.UNDER_REVIEW);
        claim.setRemark("Under Assessment");

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        ClaimResponseDTO result = claimService.getClaimById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getClaimId());
        assertEquals("UNDER_REVIEW", result.getStatus());
        assertEquals(2L, result.getPolicyId());
        assertEquals(3L, result.getUserId());
        assertEquals(8000.0, result.getClaimAmount());
        assertEquals("Medical Treatment", result.getDescription());
        assertEquals("Under Assessment", result.getRemark());
        assertEquals("Claim fetched successfully", result.getMessage());
    }

    @Test
    @DisplayName("Should throw ClaimNotFoundException when claim not found by ID")
    void testGetClaimById_NotFound() {
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ClaimNotFoundException.class, () -> claimService.getClaimById(99L));
    }

    // ==================== updateClaimStatus ====================

    @ParameterizedTest
    @CsvSource({
        "SUBMITTED, UNDER_REVIEW, UNDER_REVIEW",
        "SUBMITTED, APPROVED, APPROVED",
        "UNDER_REVIEW, REJECTED, REJECTED",
        "APPROVED, CLOSED, CLOSED",
        "REJECTED, CLOSED, CLOSED"
    })
    @DisplayName("Should transition claim status correctly")
    void testUpdateClaimStatus_Transitions(String initialStatus, String newStatus, String expectedStatus) {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.valueOf(initialStatus));
        claim.setUserId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
        dto.setStatus(newStatus);
        claimService.updateClaimStatus(1L, dto);
        assertEquals(ClaimStatus.valueOf(expectedStatus), claim.getClaimStatus());
    }

    @Test
    @DisplayName("Should throw RuntimeException for invalid status transition")
    void testUpdateClaimStatus_InvalidTransition() {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.CLOSED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
        dto.setStatus("APPROVED");
        assertThrows(RuntimeException.class, () -> claimService.updateClaimStatus(1L, dto));
    }

    @Test
    @DisplayName("Should throw RuntimeException for unrecognised status string")
    void testUpdateClaimStatus_InvalidStatusString() {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
        dto.setStatus("INVALID_STATUS");
        assertThrows(RuntimeException.class, () -> claimService.updateClaimStatus(1L, dto));
    }

    @Test
    @DisplayName("Should throw ClaimNotFoundException when updating status of non-existent claim")
    void testUpdateClaimStatus_ClaimNotFound() {
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());
        ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
        dto.setStatus("APPROVED");
        assertThrows(ClaimNotFoundException.class, () -> claimService.updateClaimStatus(99L, dto));
    }

    @Test
    @DisplayName("Should update claim status with remark when remark is provided")
    void testUpdateClaimStatus_WithRemark() {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        claim.setUserId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
        dto.setStatus("APPROVED");
        dto.setRemark("Verified and approved after inspection");
        claimService.updateClaimStatus(1L, dto);

        assertEquals(ClaimStatus.APPROVED, claim.getClaimStatus());
        assertEquals("Verified and approved after inspection", claim.getRemark());
    }

    @Test
    @DisplayName("Should send status update email when user is found")
    void testUpdateClaimStatus_SendsEmailWhenUserFound() {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        claim.setUserId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        User user = new User();
        user.setName("Jane Doe");
        user.setEmail("jane@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
        dto.setStatus("APPROVED");
        claimService.updateClaimStatus(1L, dto);

        verify(emailService, times(1)).sendHtmlEmail(anyString(), anyString(), anyString());
    }

    // ==================== getClaimsByUserId ====================

    @Test
    @DisplayName("Should return all claims for a specific user")
    void testGetClaimsByUserId_Success() {
        Claim claim1 = new Claim();
        claim1.setId(1L);
        claim1.setPolicyId(10L);
        claim1.setUserId(5L);
        claim1.setClaimAmount(1000.0);
        claim1.setDescription("Fire Damage");
        claim1.setClaimStatus(ClaimStatus.SUBMITTED);

        Claim claim2 = new Claim();
        claim2.setId(2L);
        claim2.setPolicyId(11L);
        claim2.setUserId(5L);
        claim2.setClaimAmount(2000.0);
        claim2.setDescription("Flood Damage");
        claim2.setClaimStatus(ClaimStatus.APPROVED);

        when(claimRepository.findByUserId(5L)).thenReturn(List.of(claim1, claim2));

        List<ClaimResponseDTO> result = claimService.getClaimsByUserId(5L);

        assertEquals(2, result.size());
        assertEquals("SUBMITTED", result.get(0).getStatus());
        assertEquals(1000.0, result.get(0).getClaimAmount());
        assertEquals("APPROVED", result.get(1).getStatus());
        assertEquals(2000.0, result.get(1).getClaimAmount());
    }

    @Test
    @DisplayName("Should return empty list when user has no claims")
    void testGetClaimsByUserId_EmptyList() {
        when(claimRepository.findByUserId(99L)).thenReturn(List.of());
        List<ClaimResponseDTO> result = claimService.getClaimsByUserId(99L);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getClaimStats ====================

    @Test
    @DisplayName("Should return correct claim statistics")
    void testGetClaimStats() {
        when(claimRepository.count()).thenReturn(10L);
        when(claimRepository.countByClaimStatus(ClaimStatus.SUBMITTED)).thenReturn(5L);
        when(claimRepository.countByClaimStatus(ClaimStatus.APPROVED)).thenReturn(3L);
        when(claimRepository.countByClaimStatus(ClaimStatus.REJECTED)).thenReturn(2L);

        ClaimStatsDTO stats = claimService.getClaimStats();
        assertEquals(10L, stats.getTotalClaims());
        assertEquals(5L, stats.getSubmittedClaims());
        assertEquals(3L, stats.getApprovedClaims());
        assertEquals(2L, stats.getRejectedClaims());
    }

    // ==================== getAllClaims ====================

    @Test
    @DisplayName("Should return paginated list of all claims")
    void testGetAllClaims_Success() {
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setPolicyId(1L);
        claim.setUserId(1L);
        claim.setClaimAmount(5000.0);
        claim.setDescription("Test Claim");
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Claim> page = new PageImpl<>(List.of(claim), pageable, 1);
        when(claimRepository.findAll(pageable)).thenReturn(page);

        Page<ClaimResponseDTO> result = claimService.getAllClaims(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("SUBMITTED", result.getContent().get(0).getStatus());
        assertEquals(5000.0, result.getContent().get(0).getClaimAmount());
        assertEquals("Claim fetched successfully", result.getContent().get(0).getMessage());
    }

    @Test
    @DisplayName("Should return empty page when no claims exist")
    void testGetAllClaims_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Claim> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(claimRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<ClaimResponseDTO> result = claimService.getAllClaims(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    // ==================== updateClaim ====================

    @Test
    @DisplayName("Should update claim amount, description and policyId successfully")
    void testUpdateClaim_Success() {
        Claim claim = new Claim();
        claim.setId(1L);
        claim.setPolicyId(1L);
        claim.setUserId(1L);
        claim.setClaimAmount(500.0);
        claim.setDescription("Old description");
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        ClaimRequestDTO dto = new ClaimRequestDTO();
        dto.setClaimAmount(9000.0);
        dto.setDescription("Updated description");
        dto.setPolicyId(2L);

        ClaimResponseDTO result = claimService.updateClaim(1L, dto);

        assertNotNull(result);
        assertEquals("Claim updated successfully by Admin", result.getMessage());
        assertEquals(9000.0, result.getClaimAmount());
        assertEquals("Updated description", result.getDescription());
    }



    @Test
    @DisplayName("Should throw ClaimNotFoundException when updating non-existent claim")
    void testUpdateClaim_NotFound() {
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());
        ClaimRequestDTO dto = new ClaimRequestDTO();
        assertThrows(ClaimNotFoundException.class, () -> claimService.updateClaim(99L, dto));
    }

    // ==================== deleteClaim ====================

    @Test
    @DisplayName("Should delete claim and its documents successfully")
    void testDeleteClaim_Success() {
        Claim claim = new Claim();
        claim.setId(1L);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        claimService.deleteClaim(1L);

        verify(documentRepository, times(1)).deleteByClaimId(1L);
        verify(claimRepository, times(1)).delete(claim);
    }

    @Test
    @DisplayName("Should throw ClaimNotFoundException when deleting non-existent claim")
    void testDeleteClaim_NotFound() {
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ClaimNotFoundException.class, () -> claimService.deleteClaim(99L));
    }


    @Test
    @DisplayName("Should throw RuntimeException for unsupported status transition")
    void testUpdateClaimStatus_UnsupportedTransition() {
        Claim claim = new Claim();
        claim.setClaimStatus(ClaimStatus.SUBMITTED);
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
        dto.setStatus("CLOSED"); // SUBMITTED to CLOSED is not valid in switch
        assertThrows(RuntimeException.class, () -> claimService.updateClaimStatus(1L, dto));
    }


}

