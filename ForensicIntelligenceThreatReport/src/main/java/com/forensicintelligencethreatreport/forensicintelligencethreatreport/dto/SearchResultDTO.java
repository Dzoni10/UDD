package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record SearchResultDTO(
        @JsonProperty("id")
        String id,

        @JsonProperty("title")
        String title,

        @JsonProperty("forensician_name")
        String forensicianName,

        @JsonProperty("organization")
        String organization,

        @JsonProperty("malware_name")
        String malwareName,

        @JsonProperty("threat_level")
        String threatLevel,

        @JsonProperty("hash_md5")
        String hashMD5,

        @JsonProperty("hash_sha256")
        String hashSHA256,

        @JsonProperty("content_summary")
        String contentSummary,

        @JsonProperty("highlighted_content")
        Map<String, String> highlightedContent,

        @JsonProperty("relevance_score")
        double relevanceScore
) {
}
