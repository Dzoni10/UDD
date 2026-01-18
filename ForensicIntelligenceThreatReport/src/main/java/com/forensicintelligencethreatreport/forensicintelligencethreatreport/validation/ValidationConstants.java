package com.forensicintelligencethreatreport.forensicintelligencethreatreport.validation;

public class ValidationConstants {

    // User validation patterns
    public static final String NAME_PATTERN = "^[a-zA-Z0-9À-ž\\s\\-'@]+$";
    public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";


    // Size limits
    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 128;

    // Error messages
    public static final String NAME_INVALID_MSG = "Name must contain only letters, numbers, spaces, hyphens, apostrophes and @";
    public static final String EMAIL_INVALID_MSG = "Invalid email format";

    private ValidationConstants() {
        // Prevent instantiation
    }
}
