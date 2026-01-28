package com.forensicintelligencethreatreport.forensicintelligencethreatreport.controller;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.AdvancedSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.BasicSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.SearchQueryDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.SearchResultDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/basic")
    public ResponseEntity<Page<SearchResultDTO>> basicSearch(
            @RequestBody BasicSearchRequestDTO request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Basic search request: {}", request);

        Pageable pageable = PageRequest.of(page, size);
        Page<SearchResultDTO> results = searchService.basicSearch(request, pageable);

        return ResponseEntity.ok(results);
    }

    @PostMapping("/advanced")
    public ResponseEntity<Page<SearchResultDTO>> advancedSearch(@RequestBody AdvancedSearchRequestDTO advancedSearchRequestDTO,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Advanced search request with expressions: {}", advancedSearchRequestDTO.expressions());

        Pageable pageable = PageRequest.of(page, size);
        Page<SearchResultDTO> results = searchService.advancedSearch(advancedSearchRequestDTO, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/knn")
    public ResponseEntity<Page<SearchResultDTO>> knnSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("KNN search query: {}", query);

        Pageable pageable = PageRequest.of(page, size);
        Page<SearchResultDTO> results = searchService.knnSearch(query, pageable);

        return ResponseEntity.ok(results);
    }

    @PostMapping("/phrase")
    public ResponseEntity<Page<SearchResultDTO>> phraseSearch(
            @RequestParam String phrase,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Phrase search: {}", phrase);

        Pageable pageable = PageRequest.of(page, size);
        Page<SearchResultDTO> results = searchService.phraseSearch(phrase, pageable);

        return ResponseEntity.ok(results);
    }

    @PostMapping("/fulltext")
    public ResponseEntity<Page<SearchResultDTO>> fullTextSearch(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Full-text search: {}", text);

        Pageable pageable = PageRequest.of(page, size);
        Page<SearchResultDTO> results = searchService.fullTextSearch(text, pageable);
        return ResponseEntity.ok(results);
    }


}
