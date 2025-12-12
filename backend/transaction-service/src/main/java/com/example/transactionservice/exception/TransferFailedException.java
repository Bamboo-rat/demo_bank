package com.example.transactionservice.exception;

/**
 * Exception for transfer operation failures
 */
public class TransferFailedException extends BaseException {
    
    public TransferFailedException(String message) {
        super(ErrorCode.TRANSFER_FAILED, message);
    }
    
    public TransferFailedException(String message, Throwable cause) {
        super(ErrorCode.TRANSFER_FAILED, message, cause);
    }
    
    public TransferFailedException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
