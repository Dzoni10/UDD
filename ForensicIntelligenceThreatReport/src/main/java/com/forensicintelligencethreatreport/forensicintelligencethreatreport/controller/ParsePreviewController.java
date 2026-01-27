package com.forensicintelligencethreatreport.forensicintelligencethreatreport.controller;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.IndexDocumentDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.ParsedDocumentDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.ParsePreviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/parse-preview")
@RequiredArgsConstructor
@Slf4j
public class ParsePreviewController {
    private final ParsePreviewService parsePreviewService;

    @GetMapping("/{serverFilename}")
    public ResponseEntity<ParsedDocumentDTO> parseDocument(
            @PathVariable String serverFilename) {

        log.info("Parse preview request for: {}", serverFilename);
        ParsedDocumentDTO parsed = parsePreviewService.parseDocument(serverFilename);

        return ResponseEntity.ok(parsed);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String,String>> confirmAndIndex(
            @RequestBody IndexDocumentDTO documentDTO) {

        log.info("Confirming and indexing document: {}", documentDTO.serverFilename());
        parsePreviewService.indexDocument(documentDTO);


        Map<String, String> response = new HashMap<>();
        response.put("message", "Document indexed successfully!");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{serverFilename}")
    public ResponseEntity<String> cancelAndDelete(
            @PathVariable String serverFilename) {

        log.info("Canceling and deleting document: {}", serverFilename);
        parsePreviewService.deleteDocument(serverFilename);

        return ResponseEntity
                .ok("Document deleted successfully!");
    }

}
