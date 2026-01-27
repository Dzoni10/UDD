package com.forensicintelligencethreatreport.forensicintelligencethreatreport.controller;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.DocumentUploadResponseDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping("/download/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) throws IOException {
        log.info("STATISTIC-LOG serveFile -> {}", filename);

        var minioResponse = fileService.loadAsResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        minioResponse.headers().get("Content-Disposition"))
                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(Path.of(filename)))
                .body(new InputStreamResource(minioResponse));
    }

    // Upload fajlova
    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponseDTO> uploadDocument(
            @RequestParam("file") MultipartFile file) {

        log.info("Started document upload: {}", file.getOriginalFilename());

        // Validacija - Samo PDF
        if (!file.getContentType().equals("application/pdf")) {
            log.warn("Invalid file type: {}", file.getContentType());
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        // Validacija - Veličina (50MB max)
        if (file.getSize() > 50 * 1024 * 1024) {
            log.warn("File too large: {} bytes", file.getSize());
            throw new IllegalArgumentException("File size cannot exceed 50MB");
        }

        // Generisanje unique server filename-a
        String serverFilename = UUID.randomUUID().toString();

        // Skladištenje u MinIO
        String storedFilename = fileService.store(file, serverFilename);

        log.info("Document stored successfully: {}", storedFilename);

        // Formiranje odgovora
        DocumentUploadResponseDTO response = new DocumentUploadResponseDTO(
                storedFilename,
                file.getOriginalFilename(),
                file.getSize(),
                file.getContentType(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        return ResponseEntity
                .status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String filename) {
        log.info("Deleting document: {}", filename);
        fileService.delete(filename);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/signed-url/{filename}")
    public ResponseEntity<String> getSignedUrl(@PathVariable String filename) {
        log.info("Generating signed URL for: {}", filename);
        String signedUrl = fileService.getSignedUrl(filename, 300); // 5 minuta
        return ResponseEntity.ok(signedUrl);
    }
}
