package com.group2.policy_service;

import com.group2.policy_service.aspect.LoggingAspect;
import com.group2.policy_service.config.RabbitConfig;
import com.group2.policy_service.config.RedisConfig;
import com.group2.policy_service.config.SwaggerConfig;
import com.group2.policy_service.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.models.OpenAPI;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class ApplicationInfrastructureTest {

    @Test
    public void testApplicationMain() {
        PolicyServiceApplication app = new PolicyServiceApplication();
        assertNotNull(app);
    }

    @Test
    public void testRabbitConfig() {
        RabbitConfig config = new RabbitConfig();
        DirectExchange exchange = config.exchange();
        assertEquals(RabbitConfig.EXCHANGE, exchange.getName());

        Queue pQueue = config.purchaseQueue();
        assertEquals(RabbitConfig.PURCHASE_QUEUE, pQueue.getName());

        Binding pBinding = config.purchaseBinding(pQueue, exchange);
        assertNotNull(pBinding);

        assertNotNull(config.converter());
        
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        assertNotNull(config.template(connectionFactory));
    }

    @Test
    public void testRedisConfig() {
        RedisConfig config = new RedisConfig();
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisCacheManager manager = config.cacheManager(factory);
        assertNotNull(manager);

        CacheErrorHandler handler = config.errorHandler();
        assertNotNull(handler);

        Cache cache = mock(Cache.class);
        handler.handleCacheGetError(new RuntimeException("get"), cache, "key");
        handler.handleCachePutError(new RuntimeException("put"), cache, "key", "value");
        handler.handleCacheEvictError(new RuntimeException("evict"), cache, "key");
        handler.handleCacheClearError(new RuntimeException("clear"), cache);
    }

    @Test
    public void testSwaggerConfig() {
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI api = config.customOpenAPI();
        assertNotNull(api);
        assertEquals("Policy Service API", api.getInfo().getTitle());
    }

    @Test
    public void testJwtUtil() {
        JwtUtil util = new JwtUtil();
        
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String secretString = Encoders.BASE64.encode(key.getEncoded());
        ReflectionTestUtils.setField(util, "secret", secretString);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123L);
        claims.put("role", "CUSTOMER");

        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject("test@test.com")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(key)
                .compact();

        assertEquals("test@test.com", util.extractEmail(token));
        assertEquals(123L, util.extractUserId(token));
        assertEquals("CUSTOMER", util.extractRole(token));
    }

    @Test
    public void testLoggingAspect() {
        LoggingAspect aspect = new LoggingAspect();
        JoinPoint jp = mock(JoinPoint.class);
        Signature sig = mock(Signature.class);
        when(jp.getSignature()).thenReturn(sig);
        when(sig.getDeclaringTypeName()).thenReturn("TestClass");
        when(sig.getName()).thenReturn("testMethod");
        when(jp.getArgs()).thenReturn(new Object[]{"arg1"});

        aspect.logBefore(jp);
        aspect.logAfterReturning(jp, "result");
        
        Throwable e = new RuntimeException("Test Exception");
        aspect.logAfterThrowing(jp, e);
    }

    @Test
    public void testSecurityConfig() {
        com.group2.policy_service.security.JwtFilter filter = mock(com.group2.policy_service.security.JwtFilter.class);
        com.group2.policy_service.security.SecurityConfig config = new com.group2.policy_service.security.SecurityConfig(filter);
        assertNotNull(config);
    }

    @Test
    public void testJwtFilter() throws Exception {
        JwtUtil util = mock(JwtUtil.class);
        com.group2.policy_service.security.JwtFilter filter = new com.group2.policy_service.security.JwtFilter(util);
        
        jakarta.servlet.http.HttpServletRequest request = mock(jakarta.servlet.http.HttpServletRequest.class);
        jakarta.servlet.http.HttpServletResponse response = mock(jakarta.servlet.http.HttpServletResponse.class);
        jakarta.servlet.FilterChain chain = mock(jakarta.servlet.FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        filter.doFilter(request, response, chain);
        
        verify(response).setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
    }
}
