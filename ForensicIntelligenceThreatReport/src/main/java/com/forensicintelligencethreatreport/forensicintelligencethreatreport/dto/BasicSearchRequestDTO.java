package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BasicSearchRequestDTO (

    @JsonProperty("forensician_name")
    String forensicianName,

    @JsonProperty("organization")
    String organization,

    @JsonProperty("malware_name")
    String malwareName,

    @JsonProperty("hash_value")
    String hashValue,

    @JsonProperty("threat_level")
    String threatLevel,

    @JsonProperty("search_text")
    String searchText,

    @JsonProperty("is_knn")
    boolean isKnn

){}
