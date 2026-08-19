package com.group2.claims_service.listener;

import com.group2.claims_service.dto.ClaimReviewEvent;
import com.group2.claims_service.service.ClaimService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.group2.claims_service.dto.ClaimStatusUpdateDTO;

@Component
public class ClaimReviewListener {

    private final ClaimService claimService;

    public ClaimReviewListener(ClaimService claimService) {
        this.claimService = claimService;
    }

    @RabbitListener(queues = "claim.review.queue")
    public void processClaimReview(ClaimReviewEvent event) {
        System.out.println("Processing Claim Review Event for Claim ID: " + event.getClaimId() + " to " + event.getStatus());
        
        try {
            // Map event to Status Update DTO
            ClaimStatusUpdateDTO dto = new ClaimStatusUpdateDTO();
            dto.setStatus(event.getStatus());
            dto.setRemark(event.getRemark());

            // Using claimService to ensure consistent logic and email notification
            claimService.updateClaimStatus(event.getClaimId(), dto);
            System.out.println("Successfully processed claim review for Claim ID: " + event.getClaimId());
        } catch (Exception e) {
            System.err.println("Failed to process Claim Review Event: " + e.getMessage());
        }
    }
}
