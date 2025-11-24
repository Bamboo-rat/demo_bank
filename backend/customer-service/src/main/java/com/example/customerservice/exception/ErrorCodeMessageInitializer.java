package com.example.customerservice.exception;

import com.example.commonapi.util.MessageUtils;
import org.springframework.stereotype.Component;

/**
 * Bridge component to wire MessageUtils into the ErrorCode enum at runtime.
 */
@Component
public class ErrorCodeMessageInitializer {

    public ErrorCodeMessageInitializer(MessageUtils messageUtils) {
        ErrorCode.setMessageUtils(messageUtils);
    }
}
