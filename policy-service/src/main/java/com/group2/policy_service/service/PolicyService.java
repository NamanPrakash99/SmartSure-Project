package com.group2.policy_service.service;

import java.util.List;
import com.group2.policy_service.dto.PolicyRequestDTO;
import com.group2.policy_service.dto.PolicyResponseDTO;
import com.group2.policy_service.dto.PolicyStatsDTO;
import com.group2.policy_service.dto.UserPolicyResponseDTO;
import com.group2.policy_service.entity.PolicyType;

public interface PolicyService {
    // Query Methods
    List<UserPolicyResponseDTO> getPoliciesByUserId(Long userId);
    List<UserPolicyResponseDTO> getAllUserPolicies();
    List<PolicyResponseDTO> getAllPolicies();
    List<PolicyType> getAllPolicyTypes();
    PolicyResponseDTO getPolicyById(Long policyId);
    PolicyStatsDTO getPolicyStats();
    UserPolicyResponseDTO getUserPolicyById(Long id);

    // Command Methods
    UserPolicyResponseDTO purchasePolicy(Long policyId, Long userId);
    PolicyResponseDTO createPolicy(PolicyRequestDTO dto);
    PolicyResponseDTO updatePolicy(Long id, PolicyRequestDTO dto);
    void deletePolicy(Long id);
    UserPolicyResponseDTO cancelPolicy(Long userPolicyId);
}
