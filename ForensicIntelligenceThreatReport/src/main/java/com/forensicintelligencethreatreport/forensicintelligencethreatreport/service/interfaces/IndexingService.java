package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface IndexingService {
    String indexDocument(MultipartFile documentFIle);
}
