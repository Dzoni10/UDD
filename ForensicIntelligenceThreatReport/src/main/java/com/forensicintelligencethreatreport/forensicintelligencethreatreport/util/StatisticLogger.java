package com.forensicintelligencethreatreport.forensicintelligencethreatreport.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StatisticLogger {

    public void logDocumentIndexed(
            String serverFilename,
            String forensicianName,
            String organization,
            String organizationCity,
            String organizationCountry,
            String malwareName,
            String threatLevel,
            String hashMd5,
            String hashSha256
    ) {
        String statisticLog = String.format(
                "STATISTIC-LOG [DOCUMENT_INDEXED] forensician=%s | organization=%s | city=%s | country=%s | malware=%s | threat_level=%s | md5=%s | sha256=%s | filename=%s",
                sanitize(forensicianName),
                sanitize(organization),
                sanitize(organizationCity),
                sanitize(organizationCountry),
                sanitize(malwareName),
                sanitize(threatLevel),
                sanitize(hashMd5),
                sanitize(hashSha256),
                sanitize(serverFilename)
        );
        log.info(statisticLog);
    }

    public void logGeocoding(
            String address,
            String city,
            Double latitude,
            Double longitude,
            String country,
            boolean success
    ) {
        String status = success ? "SUCCESS" : "FAILED";
        String statisticLog = String.format(
                "STATISTIC-LOG [GEOCODING_%s] address=%s | city=%s | latitude=%s | longitude=%s | country=%s",
                status,
                sanitize(address),
                sanitize(city),
                latitude != null ? latitude.toString() : "0.0",
                longitude != null ? longitude.toString() : "0.0",
                sanitize(country)
        );
        log.info(statisticLog);
    }

    public void logPdfParsed(
            String filename,
            String forensician,
            String organization,
            String address,
            String malwareName,
            String threatLevel
    ) {
        String statisticLog = String.format(
                "STATISTIC-LOG [PDF_PARSED] filename=%s | forensician=%s | organization=%s | address=%s | malware=%s | threat_level=%s",
                sanitize(filename),
                sanitize(forensician),
                sanitize(organization),
                sanitize(address),
                sanitize(malwareName),
                sanitize(threatLevel)
        );
        log.info(statisticLog);
    }
    public void logIndexingError(String filename, String errorMessage) {
        String statisticLog = String.format(
                "STATISTIC-LOG [INDEXING_ERROR] filename=%s | error=%s",
                sanitize(filename),
                sanitize(errorMessage)
        );
        log.warn(statisticLog);
    }
    private String sanitize(String value) {
        if (value == null || value.isEmpty()) {
            return "Unknown";
        }
        return value
                .replaceAll("[|;\\n\\r]", " ")  // Zameni problematičnim karakterima sa razmakom
                .replaceAll("\\s+", "_")         // Zameni razmake sa _
                .replaceAll("[()]", "")          // Ukloni zagrade
                .trim();
    }
}
