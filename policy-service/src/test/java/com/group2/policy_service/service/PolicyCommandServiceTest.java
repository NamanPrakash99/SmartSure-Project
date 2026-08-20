package com.group2.policy_service.service;

import com.group2.policy_service.config.RabbitConfig;
import com.group2.policy_service.dto.PolicyRequestDTO;
import com.group2.policy_service.dto.PolicyResponseDTO;
import com.group2.policy_service.dto.UserPolicyResponseDTO;
import com.group2.policy_service.dto.event.PolicyPurchaseEvent;
import com.group2.policy_service.entity.Policy;
import com.group2.policy_service.entity.PolicyStatus;
import com.group2.policy_service.entity.PolicyType;
import com.group2.policy_service.entity.UserPolicy;
import com.group2.policy_service.repository.PolicyRepository;
import com.group2.policy_service.repository.PolicyTypeRepository;
import com.group2.policy_service.repository.UserPolicyRepository;
import com.group2.policy_service.service.impl.PolicyCommandServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class PolicyCommandServiceTest {

    @InjectMocks
    private PolicyCommandServiceImpl policyCommandService;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private UserPolicyRepository userPolicyRepository;

    @Mock
    private PolicyTypeRepository policyTypeRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    public void setupSecurity() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(1L);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    public void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreatePolicy_Success() {
        PolicyRequestDTO dto = new PolicyRequestDTO();
        dto.setPolicyName("New Policy");
        dto.setPolicyTypeId(1L);
        dto.setPremiumAmount(100.0);

        PolicyType type = new PolicyType();
        type.setId(1L);

        Policy policy = new Policy();
        policy.setId(1L);
        policy.setPolicyName("New Policy");
        policy.setPolicyType(type);

        when(policyTypeRepository.findById(1L)).thenReturn(Optional.of(type));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        PolicyResponseDTO result = policyCommandService.createPolicy(dto);

        assertNotNull(result);
        assertEquals("New Policy", result.getPolicyName());
    }



    @Test
    public void testDeletePolicy_Success() {
        Policy policy = new Policy();
        policy.setId(1L);
        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        policyCommandService.deletePolicy(1L);

        assertFalse(policy.isActive());
        verify(policyRepository).save(policy);
    }

    @Test
    public void testUpdatePolicy_Success() {
        Policy existing = new Policy();
        existing.setId(1L);
        
        PolicyRequestDTO dto = new PolicyRequestDTO();
        dto.setPolicyName("Updated");

        when(policyRepository.findById(1L)).thenReturn(Optional.of(existing));

        PolicyResponseDTO result = policyCommandService.updatePolicy(1L, dto);
        assertEquals("Updated", result.getPolicyName());
    }

    @Test
    public void testPurchasePolicy_Success() {
        Policy policy = new Policy();
        policy.setId(10L);
        policy.setPolicyName("Health");
        policy.setDurationInMonths(12);
        policy.setPremiumAmount(500.0);

        UserPolicy saved = new UserPolicy();
        saved.setId(100L);
        saved.setUserId(1L);
        saved.setPolicy(policy);
        saved.setStatus(PolicyStatus.ACTIVE);

        when(policyRepository.findById(10L)).thenReturn(Optional.of(policy));
        when(userPolicyRepository.existsByUserIdAndPolicyIdAndStatus(1L, 10L, PolicyStatus.ACTIVE)).thenReturn(false);
        when(userPolicyRepository.save(any(UserPolicy.class))).thenReturn(saved);

        UserPolicyResponseDTO result = policyCommandService.purchasePolicy(10L);

        assertNotNull(result);
        assertEquals(PolicyStatus.ACTIVE, result.getStatus());
        verify(rabbitTemplate).convertAndSend(eq(RabbitConfig.EXCHANGE), eq(RabbitConfig.PURCHASE_ROUTING_KEY), any(PolicyPurchaseEvent.class));
    }



    @Test
    public void testCancelPolicy_Success() {
        UserPolicy up = new UserPolicy();
        up.setStatus(PolicyStatus.ACTIVE);
        up.setPolicy(new Policy());

        when(userPolicyRepository.findById(1L)).thenReturn(Optional.of(up));

        UserPolicyResponseDTO result = policyCommandService.cancelPolicy(1L);
        assertEquals(PolicyStatus.CANCELLED, result.getStatus());
    }



    @Test
    public void testRenewPolicy_Success() {
        Policy p = new Policy();
        p.setDurationInMonths(6);
        UserPolicy up = new UserPolicy();
        up.setPolicy(p);
        up.setEndDate(LocalDate.now().minusDays(1));

        when(userPolicyRepository.findById(1L)).thenReturn(Optional.of(up));

        UserPolicyResponseDTO result = policyCommandService.renewPolicy(1L);
        assertEquals(PolicyStatus.ACTIVE, result.getStatus());
    }

    @Test
    public void testDeleteUserPolicy_Success() {
        UserPolicy up = new UserPolicy();
        up.setUserId(1L);
        when(userPolicyRepository.findById(1L)).thenReturn(Optional.of(up));

        policyCommandService.deleteUserPolicy(1L);
        verify(userPolicyRepository).delete(up);
    }


}
