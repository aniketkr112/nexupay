package com.nexupay.payment.credential.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateCredentialRequest {

    @NotBlank(message = "Credential name is required")
    @Size(max = 100, message = "Credential name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Environment is required")
    @Pattern(regexp = "TEST|LIVE", message = "Environment must be TEST or LIVE")
    private String environment;

    public CreateCredentialRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment == null ? null : environment.trim();
    }
}
