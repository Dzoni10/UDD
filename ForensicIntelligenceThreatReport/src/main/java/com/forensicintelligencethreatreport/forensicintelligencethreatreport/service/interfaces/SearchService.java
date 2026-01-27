package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.AdvancedSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.BasicSearchRequestDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.SearchResultDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SearchService {

    Page<SearchResultDTO> basicSearch(BasicSearchRequestDTO request, Pageable pageable);

    //Page<DummyIndex> simpleSearch(List<String> keywords, Pageable pageable, boolean isKNN);

    Page<SearchResultDTO> advancedSearch(AdvancedSearchRequestDTO request, Pageable pageable);

    Page<SearchResultDTO> knnSearch(String query, Pageable pageable);

    Page<SearchResultDTO> phraseSearch(String phrase, Pageable pageable);

    Page<SearchResultDTO> fullTextSearch(String text, Pageable pageable);

}
