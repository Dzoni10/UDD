package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.IndexDocumentDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.ParsedDocumentDTO;

public interface ParsePreviewService {

    ParsedDocumentDTO parseDocument(String serverFilename);
    void indexDocument(IndexDocumentDTO document);
    void deleteDocument(String serverFilename);

}
