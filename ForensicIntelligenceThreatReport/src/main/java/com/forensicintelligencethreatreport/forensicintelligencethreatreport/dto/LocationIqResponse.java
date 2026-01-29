package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocationIqResponse(
        String lat,
        String lon,
        @JsonProperty("display_name")
        String displayName,
        LocationIqAddress address
) {
}
