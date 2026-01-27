package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AdvancedSearchRequestDTO(
        @JsonProperty("expressions")
        List<String> expressions,

        @JsonProperty("operators")
        List<String> operators
) {
}
