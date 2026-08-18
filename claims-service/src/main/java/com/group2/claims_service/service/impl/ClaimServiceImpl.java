package com.group2.claims_service.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.group2.claims_service.dto.ClaimCreatedEvent;
import com.group2.claims_service.dto.ClaimRequestDTO;
import com.group2.claims_service.dto.ClaimResponseDTO;
import com.group2.claims_service.dto.ClaimStatsDTO;
import com.group2.claims_service.entity.Claim;
import com.group2.claims_service.entity.ClaimDocument;
import com.group2.claims_service.entity.ClaimStatus;
import com.group2.claims_service.exception.ClaimNotFoundException;
import com.group2.claims_service.repository.ClaimDocumentRepository;
import com.group2.claims_service.repository.ClaimRepository;
import com.group2.claims_service.repository.UserRepository;
import com.group2.claims_service.service.ClaimService;
import com.group2.claims_service.service.EmailService;
import com.group2.claims_service.dto.ClaimStatusUpdateDTO;

@Service
public class ClaimServiceImpl implements ClaimService {
	
	private final ClaimRepository claimRepository;
	private final ClaimDocumentRepository documentRepository;
	private final RabbitTemplate rabbitTemplate;
	private final UserRepository userRepository;
	private final EmailService emailService;
	private final com.group2.claims_service.client.PolicyClient policyClient;
	
	public ClaimServiceImpl(ClaimRepository claimRepository, 
	                  ClaimDocumentRepository documentRepository,
	                  RabbitTemplate rabbitTemplate,
	                  UserRepository userRepository,
	                  EmailService emailService,
	                  com.group2.claims_service.client.PolicyClient policyClient) {
		this.claimRepository = claimRepository;
		this.documentRepository = documentRepository;
		this.rabbitTemplate = rabbitTemplate;
		this.userRepository = userRepository;
		this.emailService = emailService;
		this.policyClient = policyClient;
	}
	
	public ClaimResponseDTO initiateClaim(ClaimRequestDTO requestDTO) {
		
		// Validate against policy coverage amount via synchronous call to policy-service
		com.group2.claims_service.dto.UserPolicyResponseDTO policyByClient = policyClient.getUserPolicyById(
				requestDTO.getPolicyId(),
				"SmartSureSecretKey2026"
		);
		if (policyByClient != null && requestDTO.getClaimAmount() > policyByClient.getCoverageAmount()) {
			throw new RuntimeException("Claim amount (₹" + requestDTO.getClaimAmount() + ") cannot exceed your policy's coverage amount (₹" + policyByClient.getCoverageAmount() + ").");
		}
		
	    // 1. Create Claim Entity
	    Claim claim = new Claim();
	    claim.setPolicyId(requestDTO.getPolicyId());
	    claim.setUserId(requestDTO.getUserId());
	    claim.setClaimAmount(requestDTO.getClaimAmount());
	    claim.setDescription(requestDTO.getDescription());
	    claim.setClaimStatus(ClaimStatus.SUBMITTED);
	    claim.setCreatedAt(LocalDateTime.now());

	    // 2. Save to DB
	    Claim savedClaim = claimRepository.save(claim);

	    // 3. Create Event DTO (for RabbitMQ)
	    ClaimCreatedEvent event = new ClaimCreatedEvent();
	    event.setClaimId(savedClaim.getId());
	    event.setPolicyId(savedClaim.getPolicyId());
	    event.setUserId(savedClaim.getUserId());
	    event.setClaimAmount(savedClaim.getClaimAmount());

	    // 4. Send Event to RabbitMQ
	    rabbitTemplate.convertAndSend(
	            "claim.exchange",
	            "claim.created",
	            event
	    );

	    System.out.println("✅ Claim event sent to RabbitMQ for claimId: " + savedClaim.getId());

	    // 5. Prepare API Response
	    ClaimResponseDTO response = new ClaimResponseDTO();
	    response.setClaimId(savedClaim.getId());
	    response.setPolicyId(savedClaim.getPolicyId());
	    response.setUserId(savedClaim.getUserId());
	    response.setClaimAmount(savedClaim.getClaimAmount());
	    response.setDescription(savedClaim.getDescription());
	    response.setStatus(savedClaim.getClaimStatus().name());
	    response.setMessage("Claim submitted successfully");

	    // Send Claim Initiation Email
	    try {
	        userRepository.findById(savedClaim.getUserId()).ifPresentOrElse(user -> {
	            String subject = "Claim Filed Successfully - SmartSure";
	            
	            String plainBody = "Hi " + user.getName() + ",\n\n" +
	                    "Your claim has been submitted and is currently being processed by our claims adjusters.\n\n" +
	                    "Claim Details:\n" +
	                    "- Claim ID: #" + savedClaim.getId() + "\n" +
	                    "- Policy ID: " + savedClaim.getPolicyId() + "\n" +
	                    "- Estimated Amount: ₹" + String.format("%.2f", savedClaim.getClaimAmount()) + "\n" +
	                    "- Current Status: " + savedClaim.getClaimStatus() + "\n\n" +
	                    "We'll notify you via email of any status changes. You can also monitor your claim in real-time on your dashboard.\n\n" +
	                    "Thank you,\n" +
	                    "SmartSure Insurance Management";

	            emailService.sendEmail(user.getEmail(), subject, plainBody);
	            System.out.println("Email queued for claim initiation: " + user.getEmail());
	        }, () -> {
	            System.err.println("EMAIL ERROR: User not found for claim filing ID: " + savedClaim.getUserId());
	        });
	    } catch (Exception e) {
	        System.err.println("Failed to send claim initiation email: " + e.getMessage());
	    }



	    // 6. Return response
	    return response;
	}
	
	public String uploadDocument(Long claimId, MultipartFile file) {
		
		claimRepository.findById(claimId)
		.orElseThrow(()-> new ClaimNotFoundException("Claim not found with id: "+claimId));
		
		String contentType = file.getContentType();
		String filename = file.getOriginalFilename();
		boolean isValid = false;

		if (contentType != null && (contentType.startsWith("image/") || contentType.equalsIgnoreCase("application/pdf"))) {
			isValid = true;
		} else if (filename != null) {
			String lower = filename.toLowerCase();
			if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif") || lower.endsWith(".pdf")) {
				isValid = true;
			}
		}

		if (!isValid) {
			throw new IllegalArgumentException("Invalid file format. Only Image and PDF files are allowed.");
		}
		
		ClaimDocument document=new ClaimDocument();
		
		document.setClaimId(claimId);
		document.setFileUrl(file.getOriginalFilename());
		document.setDocumentType(file.getContentType());
		document.setUploadedDate(LocalDateTime.now());
		try {
			document.setFileData(file.getBytes());
		} catch (java.io.IOException e) {
			throw new RuntimeException("Failed to store file data", e);
		}
		
		documentRepository.save(document);
		
		return "Document uploaded Successfully";
	}
	
	@org.springframework.transaction.annotation.Transactional(readOnly = true)
	public ClaimDocument getClaimDocument(Long claimId) {
		return documentRepository.findFirstByClaimIdOrderByIdDesc(claimId)
			.orElseThrow(() -> new ClaimNotFoundException("Document not found for claim id: " + claimId));
	}

	
	public ClaimResponseDTO getClaimStatus(Long claimId) {
		
		Claim claim=claimRepository.findById(claimId)
				.orElseThrow(()-> new ClaimNotFoundException("Claim not found with id: "+claimId));
		
		
		ClaimResponseDTO response=new ClaimResponseDTO();
		response.setClaimId(claim.getId());
		response.setStatus(claim.getClaimStatus().name());
		response.setRemark(claim.getRemark());
		response.setMessage("Claim Status fetched Successfully");
		
		return response;
		
	}
	
	public ClaimResponseDTO getClaimById(Long claimId) {

	    Claim claim = claimRepository.findById(claimId)
	            .orElseThrow(() ->
	                    new ClaimNotFoundException("Claim not found with id: " + claimId));

	    ClaimResponseDTO response = new ClaimResponseDTO();

	    response.setClaimId(claim.getId());
	    response.setStatus(claim.getClaimStatus().name());
	    response.setMessage("Claim fetched successfully");

	    response.setPolicyId(claim.getPolicyId());
	    response.setUserId(claim.getUserId());
	    response.setClaimAmount(claim.getClaimAmount());
	    response.setDescription(claim.getDescription());
	    response.setRemark(claim.getRemark());

	    return response;
	}
	
	// Update claim status (called by Admin Service via Feign)
	public void updateClaimStatus(Long claimId, ClaimStatusUpdateDTO dto) {

		Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new ClaimNotFoundException("Claim not found with id: " + claimId));

		String newStatus = dto.getStatus();
		ClaimStatus targetStatus;
		try {
			targetStatus = ClaimStatus.valueOf(newStatus.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid claim status: " + newStatus);
		}

		// Validate lifecycle transitions
		ClaimStatus currentStatus = claim.getClaimStatus();
		boolean validTransition = switch (targetStatus) {
			case UNDER_REVIEW -> currentStatus == ClaimStatus.SUBMITTED;
			case APPROVED, REJECTED -> currentStatus == ClaimStatus.SUBMITTED || currentStatus == ClaimStatus.UNDER_REVIEW;
			case CLOSED -> currentStatus == ClaimStatus.APPROVED || currentStatus == ClaimStatus.REJECTED;
			default -> false;
		};

		if (!validTransition) {
			throw new RuntimeException(
					"Invalid status transition from " + currentStatus + " to " + targetStatus);
		}

		claim.setClaimStatus(targetStatus);
		if (dto.getRemark() != null) {
			claim.setRemark(dto.getRemark());
		}
		
		Claim savedClaim = claimRepository.save(claim);

		// Send Claim Status Update Email
		try {
			userRepository.findById(savedClaim.getUserId()).ifPresentOrElse(user -> {
				String subject = "Claim Status Updated - SmartSure";
				
				String plainBody = "Hi " + user.getName() + ",\n\n" +
						"The status of your insurance claim has been updated. Please review the changes below:\n\n" +
						"Update Details:\n" +
						"- Claim Reference: #" + savedClaim.getId() + "\n" +
						"- New Status: " + savedClaim.getClaimStatus() + "\n" +
						"- Admin Review Remark: " + (savedClaim.getRemark() != null ? savedClaim.getRemark() : "No additional remarks provided.") + "\n\n" +
						"If you have any questions regarding this update, please reply to this email or contact support.\n\n" +
						"Thank you,\n" +
						"SmartSure Insurance Management";

				emailService.sendEmail(user.getEmail(), subject, plainBody);
				System.out.println("Email queued for claim status update: " + user.getEmail());
			}, () -> {
				System.err.println("EMAIL ERROR: User not found for claim status update ID: " + savedClaim.getUserId());
			});
		} catch (Exception e) {
			System.err.println("Failed to send claim status update email: " + e.getMessage());
		}


	}
	
	// Get all claims for a specific user
	public List<ClaimResponseDTO> getClaimsByUserId(Long userId) {

		return claimRepository.findByUserId(userId)
				.stream()
				.map(claim -> {
					ClaimResponseDTO dto = new ClaimResponseDTO();
					dto.setClaimId(claim.getId());
					dto.setPolicyId(claim.getPolicyId());
					dto.setUserId(claim.getUserId());
					dto.setClaimAmount(claim.getClaimAmount());
					dto.setDescription(claim.getDescription());
					dto.setStatus(claim.getClaimStatus().name());
					dto.setRemark(claim.getRemark());
					dto.setMessage("Claim fetched successfully");
					return dto;
				})
				.toList();
	}
	
	
	public ClaimStatsDTO getClaimStats() {

	    long total = claimRepository.count();
	    long submitted = claimRepository.countByClaimStatus(ClaimStatus.SUBMITTED);
	    long approved = claimRepository.countByClaimStatus(ClaimStatus.APPROVED);
	    long rejected = claimRepository.countByClaimStatus(ClaimStatus.REJECTED);

	    ClaimStatsDTO stats = new ClaimStatsDTO();
	    stats.setTotalClaims(total);
	    stats.setSubmittedClaims(submitted);
	    stats.setApprovedClaims(approved);
	    stats.setRejectedClaims(rejected);

	    return stats;
	}

	public org.springframework.data.domain.Page<ClaimResponseDTO> getAllClaims(org.springframework.data.domain.Pageable pageable) {
		return claimRepository.findAll(pageable)
				.map(claim -> {
					ClaimResponseDTO dto = new ClaimResponseDTO();
					dto.setClaimId(claim.getId());
					dto.setPolicyId(claim.getPolicyId());
					dto.setUserId(claim.getUserId());
					dto.setClaimAmount(claim.getClaimAmount());
					dto.setDescription(claim.getDescription());
					dto.setStatus(claim.getClaimStatus().name());
					dto.setRemark(claim.getRemark());
					dto.setMessage("Claim fetched successfully");
					return dto;
				});
	}

	@org.springframework.transaction.annotation.Transactional
	public ClaimResponseDTO updateClaim(Long claimId, com.group2.claims_service.dto.ClaimRequestDTO dto) {
		com.group2.claims_service.entity.Claim claim = claimRepository.findById(claimId)
				.orElseThrow(() -> new com.group2.claims_service.exception.ClaimNotFoundException("Claim not found with id: " + claimId));
		
		if (dto.getClaimAmount() > 0) claim.setClaimAmount(dto.getClaimAmount());

		if (dto.getDescription() != null) claim.setDescription(dto.getDescription());
		if (dto.getPolicyId() != null) claim.setPolicyId(dto.getPolicyId());
		
		com.group2.claims_service.entity.Claim updated = claimRepository.save(claim);
		
		ClaimResponseDTO response = new ClaimResponseDTO();
		response.setClaimId(updated.getId());
		response.setPolicyId(updated.getPolicyId());
		response.setUserId(updated.getUserId());
		response.setClaimAmount(updated.getClaimAmount());
		response.setDescription(updated.getDescription());
		response.setStatus(updated.getClaimStatus().name());
		response.setMessage("Claim updated successfully by Admin");
		
		return response;
	}





}
