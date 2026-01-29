package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeoLocationResultDTO(
        @JsonProperty("id")
        String id,

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

        @JsonProperty("distance_km")
        double distanceKm,

        @JsonProperty("incident_count")
        int incidentCount,

        @JsonProperty("threat_level")
        String threatLevel
) {
}
