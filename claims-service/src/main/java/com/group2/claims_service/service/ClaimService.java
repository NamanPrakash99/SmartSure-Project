package com.group2.claims_service.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.group2.claims_service.dto.ClaimRequestDTO;
import com.group2.claims_service.dto.ClaimResponseDTO;
import com.group2.claims_service.dto.ClaimStatsDTO;
import com.group2.claims_service.entity.ClaimDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.group2.claims_service.dto.ClaimStatusUpdateDTO;

public interface ClaimService {
    ClaimResponseDTO initiateClaim(ClaimRequestDTO requestDTO);
    String uploadDocument(Long claimId, MultipartFile file);
    ClaimDocument getClaimDocument(Long claimId);
    ClaimResponseDTO getClaimStatus(Long claimId);
    ClaimResponseDTO getClaimById(Long claimId);
    void updateClaimStatus(Long claimId, ClaimStatusUpdateDTO dto);
    List<ClaimResponseDTO> getClaimsByUserId(Long userId);
    ClaimStatsDTO getClaimStats();
    Page<ClaimResponseDTO> getAllClaims(Pageable pageable);
    ClaimResponseDTO updateClaim(Long claimId, ClaimRequestDTO dto);
}
