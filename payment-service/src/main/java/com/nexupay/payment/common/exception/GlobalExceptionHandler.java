package com.nexupay.payment.common.exception;

import com.nexupay.payment.common.dto.ErrorResponse;
import com.nexupay.payment.common.dto.ValidationError;
import com.nexupay.payment.common.enums.ErrorCode;
import com.nexupay.payment.refund.exceptions.LargeAmountRefundException;
import com.nexupay.payment.refund.exceptions.PaymentNotEligibleForRefundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<ValidationError> errorList = ex.getBindingResult().getFieldErrors().stream()
                .map(error->{
                    ValidationError validationError = new ValidationError();
                    validationError.setField(error.getField());
                    validationError.setMessage(error.getDefaultMessage());
                    return validationError;
                }).toList();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setSuccess(false);
        errorResponse.setErrorCode(ErrorCode.VALIDATION_ERROR);
        errorResponse.setMessage("Validation failed");
        errorResponse.setErrorList(errorList);

        return ResponseEntity.badRequest()
                .body(errorResponse);
    }

    @ExceptionHandler(MerchantAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleMerchantAlreadyExists(
            MerchantAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        ErrorResponse.of(
                                ErrorCode.MERCHANT_ALREADY_EXISTS,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.PAYMENT_DOES_NOT_EXIST,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPaymentState(
            InvalidPaymentStateException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.INVALID_PAYMENT_STATUS,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(MerchantNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMerchantNotFound(
            MerchantNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.MERCHANT_NOT_FOUND,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(PaymentAttemptNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentAttemptNotFound(
            PaymentAttemptNotFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.PAYMENT_ATTEMPT_NOT_FOUND,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(PaymentExpiredException.class)
    public ResponseEntity<ErrorResponse> handlePaymentExpired(
            PaymentExpiredException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.PAYMENT_EXPIRED,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(MerchantAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleMerchantAccessDenied(
            MerchantAccessDeniedException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.PAYMENT_NOT_FOUND_BY_MERCHANT,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(LargeAmountRefundException.class)
    public ResponseEntity<ErrorResponse> handleLargeAmountRefundException(
            LargeAmountRefundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.REFUND_AMOUNT_EXCEEDS,
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(PaymentNotEligibleForRefundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotEligibleForRefundException(
            PaymentNotEligibleForRefundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        ErrorResponse.of(
                                ErrorCode.PAYMENT_NOT_ELIGIBLE_FOR_REFUND,
                                ex.getMessage()
                        )
                );
    }

}
