package com.example.transactionservice.config;

import com.example.transactionservice.exception.ExternalServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Feign Error Decoder
 * Handles errors from external service calls
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error - Method: {}, Status: {}", methodKey, response.status());

        switch (response.status()) {
            case 400:
                return new ExternalServiceException("Bad request to external service: " + methodKey);
            case 404:
                return new ExternalServiceException("Resource not found in external service: " + methodKey);
            case 500:
                return new ExternalServiceException("Internal server error in external service: " + methodKey);
            case 503:
                return new ExternalServiceException("Service unavailable: " + methodKey);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
