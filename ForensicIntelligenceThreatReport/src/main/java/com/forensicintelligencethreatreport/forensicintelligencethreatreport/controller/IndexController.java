package com.forensicintelligencethreatreport.forensicintelligencethreatreport.controller;


import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.DummyDocumentFileDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.DummyDocumentFileResponseDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.IndexingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/index")
@RequiredArgsConstructor
public class IndexController {

    private final IndexingService indexingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DummyDocumentFileResponseDTO addDocumentFile(
            @ModelAttribute DummyDocumentFileDTO documentFile) {
        var serverFilename = indexingService.indexDocument(documentFile.file());
        return new DummyDocumentFileResponseDTO(serverFilename);
    }
}
