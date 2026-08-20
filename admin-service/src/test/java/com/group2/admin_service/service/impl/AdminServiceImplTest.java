package com.group2.admin_service.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.group2.admin_service.dto.*;
import com.group2.admin_service.feign.AuthFeignClient;
import com.group2.admin_service.feign.ClaimsFeignClient;
import com.group2.admin_service.feign.PolicyFeignClient;

import feign.FeignException;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private ClaimsFeignClient claimsFeignClient;

    @Mock
    private PolicyFeignClient policyFeignClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private AuthFeignClient authFeignClient;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void testGetAllCustomers() {
        UserDTO user = new UserDTO();
        user.setId(1L);
        when(authFeignClient.getAllCustomers()).thenReturn(Arrays.asList(user));

        List<UserDTO> result = adminService.getAllCustomers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(authFeignClient).getAllCustomers();
    }

    @Test
    void testReviewClaim() {
        ReviewRequest request = new ReviewRequest();
        request.setStatus("APPROVED");
        request.setRemark("Looks good");

        adminService.reviewClaim(1L, request);

        verify(claimsFeignClient).updateClaimStatus(eq(1L), any(ClaimStatusUpdateDTO.class));
        verify(rabbitTemplate).convertAndSend(eq("claim.exchange"), eq("claim.review"), any(ClaimReviewEvent.class));
    }



    @Test
    void testGetClaimStatus() {
        ClaimStatusDTO status = new ClaimStatusDTO();
        when(claimsFeignClient.getClaimStatus(1L)).thenReturn(status);

        ClaimStatusDTO result = adminService.getClaimStatus(1L);

        assertNotNull(result);
        verify(claimsFeignClient).getClaimStatus(1L);
    }



    @Test
    void testGetClaimsByUserId() {
        when(claimsFeignClient.getClaimsByUserId(1L)).thenReturn(Collections.emptyList());

        List<ClaimDTO> result = adminService.getClaimsByUserId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }



    @Test
    void testDownloadClaimDocument() {
        byte[] content = "test".getBytes();
        ResponseEntity<byte[]> response = new ResponseEntity<>(content, HttpStatus.OK);
        when(claimsFeignClient.downloadDocument(1L)).thenReturn(response);

        ResponseEntity<byte[]> result = adminService.downloadClaimDocument(1L);

        assertNotNull(result);
        assertArrayEquals(content, result.getBody());
    }



    @Test
    void testGetAllClaims() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalPages", 1);
        data.put("totalElements", 1L);
        data.put("number", 0);
        data.put("size", 10);
        
        List<Object> content = new ArrayList<>();
        Map<String, Object> claimMap = new HashMap<>();
        claimMap.put("claimId", 1L);
        content.add(claimMap);
        
        // Add an incompatible type (String) to trigger catch block in mapContentList
        content.add("invalid_item_to_trigger_catch"); 
        
        data.put("content", content);

        when(claimsFeignClient.getAllClaims(0, 10)).thenReturn(data);

        PageResponse<ClaimDTO> result = adminService.getAllClaims(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalPages());
        // Should have 1 valid claim because null triggers catch block
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testGetAllClaims_NullData() {
        when(claimsFeignClient.getAllClaims(0, 10)).thenReturn(null);
        PageResponse<ClaimDTO> result = adminService.getAllClaims(0, 10);
        assertNotNull(result);
    }

    @Test
    void testRecoverGetAllClaims() {
        PageResponse<ClaimDTO> result = adminService.recoverGetAllClaims(0, 10, new RuntimeException("Error"));
        assertNotNull(result);
        assertNull(result.getContent());
    }

    @Test
    void testUpdateClaim() {
        ClaimDTO dto = new ClaimDTO();
        when(claimsFeignClient.updateClaim(eq(1L), any(ClaimDTO.class))).thenReturn(dto);

        ClaimDTO result = adminService.updateClaim(1L, dto);

        assertNotNull(result);
        verify(claimsFeignClient).updateClaim(1L, dto);
    }

    @Test
    void testRecoverUpdateClaim() {
        assertThrows(RuntimeException.class, () -> 
            adminService.recoverUpdateClaim(1L, new ClaimDTO(), new RuntimeException("Error"))
        );
    }

    @Test
    void testCreatePolicy() {
        PolicyRequestDTO dto = new PolicyRequestDTO();
        PolicyDTO response = new PolicyDTO();
        when(policyFeignClient.createPolicy(dto)).thenReturn(response);

        PolicyDTO result = adminService.createPolicy(dto);

        assertNotNull(result);
        verify(policyFeignClient).createPolicy(dto);
    }



    @Test
    void testUpdatePolicy() {
        PolicyRequestDTO dto = new PolicyRequestDTO();
        PolicyDTO response = new PolicyDTO();
        when(policyFeignClient.updatePolicy(1L, dto)).thenReturn(response);

        PolicyDTO result = adminService.updatePolicy(1L, dto);

        assertNotNull(result);
        verify(policyFeignClient).updatePolicy(1L, dto);
    }

    @Test
    void testRecoverUpdatePolicy() {
        assertThrows(RuntimeException.class, () -> 
            adminService.recoverUpdatePolicy(1L, new PolicyRequestDTO(), new RuntimeException("Error"))
        );
    }

    @Test
    void testDeletePolicy() {
        adminService.deletePolicy(1L);
        verify(policyFeignClient).deletePolicy(1L);
    }

    @Test
    void testRecoverDeletePolicy() {
        assertThrows(RuntimeException.class, () -> 
            adminService.recoverDeletePolicy(1L, new RuntimeException("Error"))
        );
    }

    @Test
    void testGetUserPolicies() {
        when(policyFeignClient.getUserPolicies(1L)).thenReturn(Collections.emptyList());
        List<Object> result = adminService.getUserPolicies(1L);
        assertNotNull(result);
    }

    @Test
    void testGetAllUserPolicies() {
        when(policyFeignClient.getAllUserPolicies()).thenReturn(Collections.emptyList());
        List<Object> result = adminService.getAllUserPolicies();
        assertNotNull(result);
    }

    @Test
    void testRecoverGetUserPolicies() {
        List<Object> result = adminService.recoverGetUserPolicies(1L, new RuntimeException("Error"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCancelPolicy() {
        when(policyFeignClient.cancelPolicy(1L)).thenReturn(new Object());
        Object result = adminService.cancelPolicy(1L);
        assertNotNull(result);
    }

    @Test
    void testRecoverCancelPolicy() {
        assertThrows(RuntimeException.class, () -> 
            adminService.recoverCancelPolicy(1L, new RuntimeException("Error"))
        );
    }

    @Test
    void testGetReports() {
        ClaimStatusDTO claimStats = new ClaimStatusDTO();
        // Test getSafeInt with non-integers if possible, or just ensure coverage
        claimStats.setTotalClaims(10);
        claimStats.setApprovedClaims(5);
        claimStats.setRejectedClaims(5);

        PolicyStatsDTO policyStats = new PolicyStatsDTO();
        policyStats.setTotalPolicies(20);
        policyStats.setTotalRevenue(1000.0);

        when(claimsFeignClient.getClaimStats()).thenReturn(claimStats);
        when(policyFeignClient.getPolicyStats()).thenReturn(policyStats);

        ReportResponse result = adminService.getReports();

        assertNotNull(result);
        assertEquals(10, result.getTotalClaims());
        assertEquals(20, result.getTotalPolicies());
        assertEquals(1000.0, result.getTotalRevenue());
    }

    @Test
    void testRecoverGetReports() {
        ReportResponse result = adminService.recoverGetReports(new RuntimeException("Error"));
        assertNotNull(result);
        assertEquals(0, result.getTotalClaims());
    }

    @Test
    void testDeleteClaim() {
        adminService.deleteClaim(1L);
        verify(claimsFeignClient).deleteClaim(1L);
    }

    @Test
    void testRecoverDeleteClaim() {
        assertThrows(RuntimeException.class, () -> 
            adminService.recoverDeleteClaim(1L, new RuntimeException("Error"))
        );
    }
}
