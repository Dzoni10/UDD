package com.forensicintelligencethreatreport.forensicintelligencethreatreport.controller;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.SearchQueryDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/simple")
    public Page<DummyIndex> simpleSearch(@RequestParam Boolean isKnn,
                                         @RequestBody SearchQueryDTO simpleSearchQuery,
                                         Pageable pageable) {
        return searchService.simpleSearch(simpleSearchQuery.keywords(), pageable, isKnn);
    }

    @PostMapping("/advanced")
    public Page<DummyIndex> advancedSearch(@RequestBody SearchQueryDTO advancedSearchQuery,
                                           Pageable pageable) {
        return searchService.advancedSearch(advancedSearchQuery.keywords(), pageable);
    }
}
