package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LocationIqAddress(
        String country,
        String city,
        String town,
        String village,
        String county
) {}
