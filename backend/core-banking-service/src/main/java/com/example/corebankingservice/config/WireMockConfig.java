package com.example.corebankingservice.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
@ConditionalOnProperty(name = "wiremock.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class WireMockConfig {

    @Value("${wiremock.port:8090}")
    private int wireMockPort;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        WireMockServer wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .port(wireMockPort)
                        .usingFilesUnderDirectory("src/main/resources/wiremock")
        );

        log.info("Starting WireMock server on port {}", wireMockPort);
        
        // Setup default stubs
        setupDefaultStubs(wireMockServer);
        
        return wireMockServer;
    }

    private void setupDefaultStubs(WireMockServer server) {
        // Stub: Verify account at ACB Bank
        server.stubFor(post(urlPathEqualTo("/api/partner/acb/verify-account"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "accountNumber": "1234567890",
                                "accountName": "NGUYEN VAN A",
                                "bankCode": "ACB",
                                "bankName": "Ngân hàng TMCP Á Châu",
                                "exists": true,
                                "active": true,
                                "message": "Account verified successfully"
                            }
                        """)));

        // Stub: Verify account at VCB Bank
        server.stubFor(post(urlPathEqualTo("/api/partner/vcb/verify-account"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "accountNumber": "0987654321",
                                "accountName": "TRAN THI B",
                                "bankCode": "VCB",
                                "bankName": "Ngân hàng TMCP Ngoại Thương Việt Nam",
                                "exists": true,
                                "active": true,
                                "message": "Account verified successfully"
                            }
                        """)));

        // Stub: Account not found
        server.stubFor(post(urlPathMatching("/api/partner/.*/verify-account"))
                .withRequestBody(containing("\"accountNumber\":\"0000000000\""))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "accountNumber": "0000000000",
                                "accountName": null,
                                "bankCode": null,
                                "bankName": null,
                                "exists": false,
                                "active": false,
                                "message": "Account not found"
                            }
                        """)));

        // Stub: Transfer to partner bank (ACB)
        server.stubFor(post(urlPathEqualTo("/api/partner/acb/transfer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "transactionId": "ACB_TX_${__UUID()}",
                                "status": "SUCCESS",
                                "message": "Transfer completed successfully",
                                "timestamp": "${__NOW()}"
                            }
                        """)));

        // Stub: Transfer to partner bank (VCB)
        server.stubFor(post(urlPathEqualTo("/api/partner/vcb/transfer"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "transactionId": "VCB_TX_${__UUID()}",
                                "status": "SUCCESS",
                                "message": "Transfer completed successfully",
                                "timestamp": "${__NOW()}"
                            }
                        """)));

        // Stub: Get account balance from partner bank
        server.stubFor(get(urlPathMatching("/api/partner/.*/balance/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "accountNumber": "1234567890",
                                "balance": "5000000",
                                "currency": "VND",
                                "status": "ACTIVE"
                            }
                        """)));

        log.info("WireMock default stubs configured successfully");
    }
}
