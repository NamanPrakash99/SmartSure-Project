package com.group2.claims_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import com.group2.claims_service.dto.ClaimRequestDTO;
import com.group2.claims_service.dto.ClaimResponseDTO;
import com.group2.claims_service.dto.ClaimStatsDTO;
import com.group2.claims_service.dto.ClaimStatusUpdateDTO;
import com.group2.claims_service.service.ClaimService;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {
	
	private final ClaimService claimService;

	public ClaimController(ClaimService claimService) {
		this.claimService = claimService;
	}
	
	
	@PostMapping("/initiate")
	@PreAuthorize("hasRole('ADMIN') or principal == #requestDTO.userId")
	public ResponseEntity<ClaimResponseDTO> initiateClaim(@RequestBody ClaimRequestDTO requestDTO){
		
		ClaimResponseDTO response=claimService.initiateClaim(requestDTO);
		
		return ResponseEntity.ok(response);
	}
	
	
	@PostMapping(value = "/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<String> uploadDocument(@RequestParam("claimId") Long claimId, @RequestParam("file") MultipartFile file){
		
		String response=claimService.uploadDocument(claimId, file);
		
		return ResponseEntity.ok(response);
	}
	
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	@GetMapping(value = "/{claimId}/document", produces = { 
		"application/pdf", "image/jpeg", "image/png", "application/octet-stream" 
	})
	public ResponseEntity<byte[]> downloadDocument(@PathVariable("claimId") Long claimId) {
		com.group2.claims_service.entity.ClaimDocument document = claimService.getClaimDocument(claimId);
		
		return ResponseEntity.ok()
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileUrl() + "\"")
				.header(org.springframework.http.HttpHeaders.CONTENT_TYPE, document.getDocumentType() != null ? document.getDocumentType() : "application/octet-stream")
				.body(document.getFileData());
	}
	
	@GetMapping("/status/{claimId}")
	public ResponseEntity<ClaimResponseDTO> getClaimStatus(@PathVariable("claimId") Long claimId){
		
		ClaimResponseDTO response=claimService.getClaimStatus(claimId);
		
		return ResponseEntity.ok(response);
	}
	
	@PutMapping("/{claimId}/status")
	public ResponseEntity<String> updateClaimStatus(
			@PathVariable("claimId") Long claimId,
			@RequestBody ClaimStatusUpdateDTO dto) {
		
		claimService.updateClaimStatus(claimId, dto);
		return ResponseEntity.ok("Claim status updated successfully");
	}
	
	// Get all claims for a specific user
	@GetMapping("/user/{userId}")
	@PreAuthorize("hasRole('ADMIN') or principal == #userId")
	public ResponseEntity<List<ClaimResponseDTO>> getClaimsByUserId(@PathVariable("userId") Long userId) {
		return ResponseEntity.ok(claimService.getClaimsByUserId(userId));
	}

	// Get all claims with pagination
	@GetMapping("/admin/all")
	@PreAuthorize("hasRole('ADMIN')")

	public ResponseEntity<java.util.Map<String, Object>> getAllClaims(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {
		org.springframework.data.domain.Page<ClaimResponseDTO> claimPage = claimService.getAllClaims(org.springframework.data.domain.PageRequest.of(page, size));
		
		java.util.Map<String, Object> response = new java.util.HashMap<>();
		response.put("content", claimPage.getContent());
		response.put("totalPages", claimPage.getTotalPages());
		response.put("totalElements", claimPage.getTotalElements());
		response.put("number", claimPage.getNumber());
		response.put("size", claimPage.getSize());
		
		return ResponseEntity.ok(response);
	}



}
