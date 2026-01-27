package com.forensicintelligencethreatreport.forensicintelligencethreatreport.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class FileValidationUtil {


    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String ALLOWED_MIME_TYPE = "application/pdf";

    public static void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validacija MIME tipa
        if (!ALLOWED_MIME_TYPE.equals(file.getContentType())) {
            log.warn("Invalid MIME type: {}", file.getContentType());
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        // Validacija veličine
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File too large: {} MB", file.getSize() / (1024 * 1024));
            throw new IllegalArgumentException(
                    "File size cannot exceed " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB"
            );
        }

        // Validacija extension-a
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("File must have .pdf extension");
        }

        log.info("File validation passed: {}", originalFilename);
    }
}
