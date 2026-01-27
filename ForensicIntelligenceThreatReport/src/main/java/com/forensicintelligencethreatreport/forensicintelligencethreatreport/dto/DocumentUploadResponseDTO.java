package com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DocumentUploadResponseDTO(
        @JsonProperty("server_filename")
        String serverFilename,

        @JsonProperty("document_name")
        String documentName,

        @JsonProperty("file_size")
        long fileSize,

        @JsonProperty("mime_type")
        String mimeType,

        @JsonProperty("upload_timestamp")
        String uploadTimestamp
) {}
