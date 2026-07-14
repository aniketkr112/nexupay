package com.nexupay.payment.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nexupay.payment.common.enums.ErrorCode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean success;
    private ErrorCode errorCode;
    private String message;
    private List<ValidationError> errorList;



    public ErrorResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ValidationError> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<ValidationError> errorList) {
        this.errorList = errorList;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        ErrorResponse response = new ErrorResponse();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;

    }
}
