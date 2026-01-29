package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GeoLocationSearchRequestDTO(
        @JsonProperty("city_or_address")
        String cityOrAddress,

        @JsonProperty("latitude")
        double latitude,

        @JsonProperty("longitude")
        double longitude,

        @JsonProperty("radius_km")
        double radiusKm
) {
}
