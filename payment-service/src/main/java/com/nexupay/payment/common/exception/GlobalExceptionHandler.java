package com.nexupay.payment.common.exception;

import com.nexupay.payment.common.dto.ErrorResponse;
import com.nexupay.payment.common.dto.ValidationError;
import com.nexupay.payment.common.enums.ErrorCode;
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

}
