package com.group2.policy_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.group2.policy_service.dto.PolicyRequestDTO;
import com.group2.policy_service.dto.PolicyResponseDTO;
import com.group2.policy_service.dto.PolicyStatsDTO;
import com.group2.policy_service.dto.UserPolicyResponseDTO;
import com.group2.policy_service.entity.PolicyType;
import com.group2.policy_service.service.PolicyService;

@RestController
@RequestMapping("/api")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }
    
    // --- QUERY ENDPOINTS ---
    
    @GetMapping("/policies")
    public List<PolicyResponseDTO> getAllPolicies() {
        return policyService.getAllPolicies();
    }
    
    @GetMapping("/policy-types")
    public List<PolicyType> getAllPolicyTypes() {
        return policyService.getAllPolicyTypes();
    }

    @GetMapping("/policies/{policyId}")
    public PolicyResponseDTO getPolicy(@PathVariable("policyId") Long policyId) {
        return policyService.getPolicyById(policyId);
    }
    
    @GetMapping("/admin/user-policies/{userId}")
    public List<UserPolicyResponseDTO> getUserPolicies(@PathVariable("userId") Long userId,
            @RequestHeader(value="X-User-Role", required=false) String role,
            @RequestHeader(value="X-User-Id", required=false) String headerUserId) {
        if (!"ADMIN".equals(role) && (headerUserId == null || !headerUserId.equals(userId.toString()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
        return policyService.getPoliciesByUserId(userId);
    }

    @GetMapping("/admin/user-policies/all")
    public List<UserPolicyResponseDTO> getAllUserPolicies() {
        return policyService.getAllUserPolicies();
    }

    // --- COMMAND ENDPOINTS ---

    @PostMapping("/policies/purchase")
    public UserPolicyResponseDTO purchasePolicy(@RequestParam("policyId") Long policyId,
            @RequestHeader(value="X-User-Id", required=true) String userId) {
        return policyService.purchasePolicy(policyId, Long.parseLong(userId));
    }

    @PostMapping("/admin/policies")
    public PolicyResponseDTO createPolicy(@RequestBody PolicyRequestDTO dto,
            @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        return policyService.createPolicy(dto);
    }

    @PutMapping("/admin/policies/{id}")
    public PolicyResponseDTO updatePolicy(@PathVariable("id") Long id,
                                          @RequestBody PolicyRequestDTO dto,
                                          @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        return policyService.updatePolicy(id, dto);
    }

    @DeleteMapping("/admin/policies/{id}")
    public void deletePolicy(@PathVariable("id") Long id,
            @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        policyService.deletePolicy(id);
    }

    @PutMapping("/admin/policies/{id}/cancel")
    public ResponseEntity<UserPolicyResponseDTO> cancelPolicy(@PathVariable("id") Long id,
            @RequestHeader(value="X-User-Role", required=false) String role) {
        if (!"ADMIN".equals(role)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        return ResponseEntity.ok(policyService.cancelPolicy(id));
    }
}
