package com.group2.policy_service.service.impl;

import com.group2.policy_service.config.RabbitConfig;
import com.group2.policy_service.dto.event.PaymentStatusEvent;
import com.group2.policy_service.entity.PolicyStatus;
import com.group2.policy_service.entity.UserPolicy;
import com.group2.policy_service.repository.UserPolicyRepository;
import com.group2.policy_service.service.SagaConsumerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SagaConsumerServiceImpl implements SagaConsumerService {

    @Autowired
    private UserPolicyRepository userPolicyRepository;

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    public void consumePaymentStatus(PaymentStatusEvent event) {
        // No-op: Payment service has been removed and policies are activated directly.
    }
}
