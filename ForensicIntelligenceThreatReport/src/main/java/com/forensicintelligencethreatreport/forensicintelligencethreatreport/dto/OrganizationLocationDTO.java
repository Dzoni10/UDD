package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OrganizationLocationDTO(
        @JsonProperty("organization")
        String organization,

        @JsonProperty("city")
        String city,

        @JsonProperty("country")
        String country,

        @JsonProperty("latitude")
        double latitude,

        @JsonProperty("longitude")
        double longitude,

        @JsonProperty("incident_count")
        int incidentCount,

        @JsonProperty("threat_level")
        String threatLevel
) {
}
