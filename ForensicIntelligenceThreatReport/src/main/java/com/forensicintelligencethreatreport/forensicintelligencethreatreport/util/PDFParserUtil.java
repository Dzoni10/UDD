package com.forensicintelligencethreatreport.forensicintelligencethreatreport.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PDFParserUtil {

    private static final Pattern FORENSICIAN_PATTERN = Pattern.compile(
            "(?i)(forensician|analyst|author|извештач|forenzičar|forenzicar|форензичар|аутор|autor)\\s*[:\\-]?\\s*([A-Za-zА-Яа-я\\s]+?)(?=\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern ORGANIZATION_PATTERN = Pattern.compile(
            "(?i)(cert|csirt|organization|agency|organizacija|организација|agencija|агенција)\\s*[:\\-]?\\s*([A-Za-zА-Яа-я0-9\\.\\s]+?)(?=\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern MALWARE_PATTERN = Pattern.compile(
            "(?i)(malware|threat|virus|trojan|trojanac|malver|малвер|тројанац|тројан|pretnja|претња)\\s*[:\\-]?\\s*([A-Za-zА-Яа-я0-9\\.\\s\\-_]+?)(?=\\n|$)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern THREAT_LEVEL_PATTERN = Pattern.compile(
            "(?i)(threat level|severity|risk|level|klasifikacija|ризик|rizik|pretnja|претња|nivo|ниво)\\s*[:\\-]?\\s*(critical|high|medium|low|kritična|visoka|srednja|niska)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern MD5_PATTERN = Pattern.compile(
            "(?i)(md5|hash.*md5)\\s*[:\\-]?\\s*([a-fA-F0-9]{32})"
    );

    private static final Pattern SHA256_PATTERN = Pattern.compile(
            "(?i)(sha-?256|hash.*sha)\\s*[:\\-]?\\s*([a-fA-F0-9]{64})"
    );

    // Ekstraktuj tekst iz PDF-a
    public static String extractTextFromPDF(InputStream inputStream) throws IOException {
        try (PDDocument pdDocument = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(pdDocument);
        }
    }

    // Ekstraktuj forenzičara
    public static String extractForensician(String pdfText) {
        return extractPatternValue(pdfText, FORENSICIAN_PATTERN);
    }

    // Ekstraktuj organizaciju
    public static String extractOrganization(String pdfText) {
        return extractPatternValue(pdfText, ORGANIZATION_PATTERN);
    }

    // Ekstraktuj malver
    public static String extractMalware(String pdfText) {
        return extractPatternValue(pdfText, MALWARE_PATTERN);
    }

    // Ekstraktuj nivo pretnje
    public static String extractThreatLevel(String pdfText) {
        String extracted = extractPatternValue(pdfText, THREAT_LEVEL_PATTERN);
        return normalizeThreatLevel(extracted);
    }

    // Ekstraktuj MD5
    public static String extractMD5(String pdfText) {
        return extractPatternValue(pdfText, MD5_PATTERN);
    }

    // Ekstraktuj SHA256
    public static String extractSHA256(String pdfText) {
        return extractPatternValue(pdfText, SHA256_PATTERN);
    }

    // Ekstraktuj prvi 500 karaktera kao opis
    public static String extractDescription(String pdfText, String malwareName) {
        int malwareIndex = pdfText.toLowerCase().indexOf(
                malwareName != null ? malwareName.toLowerCase() : "malware"
        );

        if (malwareIndex != -1) {
            int endIndex = Math.min(malwareIndex + 500, pdfText.length());
            return pdfText.substring(malwareIndex, endIndex).trim();
        }

        return pdfText.substring(0, Math.min(500, pdfText.length())).trim();
    }

    // HELPER: Ekstraktuj vrednost iz regex pattern-a
    private static String extractPatternValue(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(matcher.groupCount());
            return value != null ? value.trim() : "";
        }
        return "";
    }

    // HELPER: Normalizuj nivo pretnje
    private static String normalizeThreatLevel(String level) {
        if (level.isEmpty()) return "medium";

        String normalized = level.toLowerCase();
        if (normalized.contains("critical") || normalized.contains("критична") || normalized.contains("kritična") ||normalized.contains("kriticna")) return "critical";
        if (normalized.contains("high") || normalized.contains("visoka") ||normalized.contains("висока")) return "high";
        if (normalized.contains("medium") || normalized.contains("srednja")||normalized.contains("средња")) return "medium";
        if (normalized.contains("low") || normalized.contains("niska")||normalized.contains("ниска")) return "low";

        return "medium";
    }
}
