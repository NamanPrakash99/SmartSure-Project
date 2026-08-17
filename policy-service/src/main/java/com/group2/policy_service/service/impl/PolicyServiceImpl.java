package com.group2.policy_service.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.group2.policy_service.config.RabbitConfig;
import com.group2.policy_service.dto.PolicyRequestDTO;
import com.group2.policy_service.dto.PolicyResponseDTO;
import com.group2.policy_service.dto.PolicyStatsDTO;
import com.group2.policy_service.dto.UserPolicyResponseDTO;
import com.group2.policy_service.dto.event.PolicyPurchaseEvent;
import com.group2.policy_service.entity.Policy;
import com.group2.policy_service.entity.PolicyStatus;
import com.group2.policy_service.entity.PolicyType;
import com.group2.policy_service.entity.UserPolicy;
import com.group2.policy_service.repository.PolicyRepository;
import com.group2.policy_service.repository.PolicyTypeRepository;
import com.group2.policy_service.repository.UserPolicyRepository;
import com.group2.policy_service.service.PolicyService;

@Service
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final UserPolicyRepository userPolicyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final RabbitTemplate rabbitTemplate;

    public PolicyServiceImpl(PolicyRepository policyRepository,
                               UserPolicyRepository userPolicyRepository,
                               PolicyTypeRepository policyTypeRepository,
                               RabbitTemplate rabbitTemplate) {
        this.policyRepository = policyRepository;
        this.userPolicyRepository = userPolicyRepository;
        this.policyTypeRepository = policyTypeRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ================= QUERY METHODS =================

    @Cacheable(value = "userPolicies", key = "#userId")
    public List<UserPolicyResponseDTO> getPoliciesByUserId(Long userId) {
        return userPolicyRepository.findByUserId(userId)
                .stream()
                .map(this::mapToUserPolicyResponse)
                .toList();
    }

    public List<UserPolicyResponseDTO> getAllUserPolicies() {
        return userPolicyRepository.findAll()
                .stream()
                .map(this::mapToUserPolicyResponse)
                .toList();
    }

    @Cacheable(value = "allPolicies")
    public List<PolicyResponseDTO> getAllPolicies() {
        return policyRepository.findByActiveTrue()
                .stream()
                .map(this::mapToPolicyResponse)
                .toList();
    }

    @Cacheable(value = "policyTypes")
    public List<PolicyType> getAllPolicyTypes() {
        return policyTypeRepository.findAll();
    }

    @Cacheable(value = "policyDetails", key = "#policyId")
    public PolicyResponseDTO getPolicyById(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        return mapToPolicyResponse(policy);
    }

    public PolicyStatsDTO getPolicyStats() {
        long totalPolicies = userPolicyRepository.count();
        Double revenue = userPolicyRepository.sumPremiumAmount();
        double totalRevenue = (revenue != null) ? revenue : 0.0;

        PolicyStatsDTO stats = new PolicyStatsDTO();
        stats.setTotalPolicies(totalPolicies);
        stats.setTotalRevenue(totalRevenue);
        return stats;
    }

    public UserPolicyResponseDTO getUserPolicyById(Long id) {
        UserPolicy userPolicy = userPolicyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserPolicy not found with id: " + id));
        return mapToUserPolicyResponse(userPolicy);
    }

    // ================= COMMAND METHODS =================

    @CacheEvict(value = "userPolicies", allEntries = true)
    public UserPolicyResponseDTO purchasePolicy(Long policyId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (userPolicyRepository.existsByUserIdAndPolicyIdAndStatus(userId, policyId, PolicyStatus.ACTIVE)) {
            throw new RuntimeException("You already have an active subscription for " + policy.getPolicyName());
        }

        UserPolicy userPolicy = new UserPolicy();
        userPolicy.setUserId(userId);
        userPolicy.setPolicy(policy);
        userPolicy.setStatus(PolicyStatus.ACTIVE);
        userPolicy.setStartDate(LocalDate.now());
        userPolicy.setEndDate(LocalDate.now().plusMonths(policy.getDurationInMonths()));
        userPolicy.setPremiumAmount(policy.getPremiumAmount());
        userPolicy.setNextPaymentDueDate(LocalDate.now().plusMonths(1));

        UserPolicy savedPolicy = userPolicyRepository.save(userPolicy);

        PolicyPurchaseEvent event = new PolicyPurchaseEvent(
                savedPolicy.getId(), 
                userId, 
                policyId, 
                policy.getPremiumAmount()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.PURCHASE_ROUTING_KEY, event);

        return mapToUserPolicyResponse(savedPolicy);
    }

    @CacheEvict(value = {"allPolicies", "policyDetails", "policyTypes"}, allEntries = true)
    public PolicyResponseDTO createPolicy(PolicyRequestDTO dto) {
        if (dto.getPolicyName() == null || dto.getPolicyName().trim().isEmpty()) {
            throw new RuntimeException("Policy Name is required");
        }
        if (dto.getPolicyTypeId() == null) {
            throw new RuntimeException("Policy Type ID is required");
        }

        PolicyType type = policyTypeRepository.findById(dto.getPolicyTypeId())
                .orElseThrow(() -> new RuntimeException("PolicyType not found with ID: " + dto.getPolicyTypeId()));

        Policy policy = new Policy();
        policy.setPolicyName(dto.getPolicyName());
        policy.setDescription(dto.getDescription());
        policy.setPolicyType(type);
        policy.setPremiumAmount(dto.getPremiumAmount());
        policy.setCoverageAmount(dto.getCoverageAmount());
        policy.setDurationInMonths(dto.getDurationInMonths());
        policy.setActive(true);
        policyRepository.save(policy);
        return mapToPolicyResponse(policy);
    }

    @CacheEvict(value = {"allPolicies", "policyDetails"}, allEntries = true)
    public PolicyResponseDTO updatePolicy(Long id, PolicyRequestDTO dto) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        policy.setPolicyName(dto.getPolicyName());
        policy.setDescription(dto.getDescription());
        policy.setPremiumAmount(dto.getPremiumAmount());
        policy.setCoverageAmount(dto.getCoverageAmount());
        policy.setDurationInMonths(dto.getDurationInMonths());

        policyRepository.save(policy);
        return mapToPolicyResponse(policy);
    }

    @CacheEvict(value = {"allPolicies", "policyDetails"}, allEntries = true)
    public void deletePolicy(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        policy.setActive(false);
        policyRepository.save(policy);
    }

    @CacheEvict(value = "userPolicies", allEntries = true)
    public UserPolicyResponseDTO cancelPolicy(Long userPolicyId) {
        UserPolicy userPolicy = userPolicyRepository.findById(userPolicyId)
                .orElseThrow(() -> new RuntimeException("UserPolicy not found with id: " + userPolicyId));

        if (userPolicy.getStatus() != PolicyStatus.ACTIVE) {
            throw new RuntimeException("Only ACTIVE policies can be cancelled. Current status: " + userPolicy.getStatus());
        }

        userPolicy.setStatus(PolicyStatus.CANCELLED);
        userPolicyRepository.save(userPolicy);

        return mapToUserPolicyResponse(userPolicy);
    }

    // ================= MAPPERS =================

    private PolicyResponseDTO mapToPolicyResponse(Policy policy) {
        PolicyResponseDTO dto = new PolicyResponseDTO();
        dto.setId(policy.getId());
        dto.setPolicyName(policy.getPolicyName());
        dto.setDescription(policy.getDescription());
        dto.setPremiumAmount(policy.getPremiumAmount());
        dto.setCoverageAmount(policy.getCoverageAmount());
        dto.setDurationInMonths(policy.getDurationInMonths());
        
        if (policy.getPolicyType() != null && policy.getPolicyType().getCategory() != null) {
            dto.setCategory(policy.getPolicyType().getCategory().name());
            dto.setPolicyTypeId(policy.getPolicyType().getId());
        }
        
        return dto;
    }

    private UserPolicyResponseDTO mapToUserPolicyResponse(UserPolicy userPolicy) {
        UserPolicyResponseDTO dto = new UserPolicyResponseDTO();
        dto.setId(userPolicy.getId());
        dto.setUserId(userPolicy.getUserId());
        dto.setPolicyName(userPolicy.getPolicy().getPolicyName());
        dto.setStatus(userPolicy.getStatus());
        dto.setPremiumAmount(userPolicy.getPremiumAmount());
        dto.setCoverageAmount(userPolicy.getPolicy().getCoverageAmount());
        dto.setStartDate(userPolicy.getStartDate());
        dto.setEndDate(userPolicy.getEndDate());
        dto.setPolicyId(userPolicy.getPolicy().getId());
        dto.setNextPaymentDueDate(userPolicy.getNextPaymentDueDate());
        return dto;
    }
}
