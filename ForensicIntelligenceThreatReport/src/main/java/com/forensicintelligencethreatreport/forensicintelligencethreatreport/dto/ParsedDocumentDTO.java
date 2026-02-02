package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParsedDocumentDTO (
        @JsonProperty("server_filename")
        String serverFilename,

        @JsonProperty("title")
        String title,

        @JsonProperty("forensician_name")
        String forensicianName,

        @JsonProperty("organization")
        String organization,

        @JsonProperty("organization_address")  // ✅ NOVO
        String organizationAddress,


        @JsonProperty("malware_name")
        String malwareName,

        @JsonProperty("malware_description")
        String malwareDescription,

        @JsonProperty("threat_level")
        String threatLevel,

        @JsonProperty("hash_md5")
        String hashMD5,

        @JsonProperty("hash_sha256")
        String hashSHA256,

        @JsonProperty("confidence_score")
        double confidenceScore
){}
