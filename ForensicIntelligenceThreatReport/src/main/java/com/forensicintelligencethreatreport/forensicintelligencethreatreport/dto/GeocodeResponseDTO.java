package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeocodeResponseDTO (
        @JsonProperty("latitude")
        double latitude,

        @JsonProperty("longitude")
        double longitude,

        @JsonProperty("city")
        String city,

        @JsonProperty("country")
        String country


) {
}
