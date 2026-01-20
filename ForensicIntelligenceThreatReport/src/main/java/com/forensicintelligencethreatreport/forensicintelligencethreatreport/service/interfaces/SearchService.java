package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SearchService {

    Page<DummyIndex> simpleSearch(List<String> keywords, Pageable pageable, boolean isKNN);
    Page<DummyIndex> advancedSearch(List<String> expression, Pageable pageable);
}
