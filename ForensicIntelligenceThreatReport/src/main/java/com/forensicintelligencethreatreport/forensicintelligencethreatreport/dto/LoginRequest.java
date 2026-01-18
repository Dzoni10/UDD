package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.validation.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = ValidationConstants.MIN_PASSWORD_LENGTH,
            message = "Password must be at least {min} characters")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
