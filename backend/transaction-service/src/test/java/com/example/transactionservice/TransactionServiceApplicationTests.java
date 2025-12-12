package com.example.transactionservice;

import com.example.transactionservice.client.CoreBankingClient;
import com.example.transactionservice.dubbo.consumer.AccountServiceClient;
import com.example.transactionservice.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {
    RedisAutoConfiguration.class,
    RedisReactiveAutoConfiguration.class
})
class TransactionServiceApplicationTests {

    @MockBean
    private JwtDecoder jwtDecoder;
    
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;
    
    @MockBean
    private RedisTemplate<String, String> redisTemplate;
    
    @MockBean
    private OtpService otpService;
    
    @MockBean
    private CoreBankingClient coreBankingClient;
    
    @MockBean
    private AccountServiceClient accountServiceClient;

    @Test
    void contextLoads() {
        // Context loads successfully with mocked dependencies
    }
}
