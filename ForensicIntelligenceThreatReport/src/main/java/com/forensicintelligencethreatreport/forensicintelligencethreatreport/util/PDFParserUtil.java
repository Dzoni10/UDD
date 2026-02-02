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
            "(?i)(forensician|analyst|author|извештач|forenzičar|forenzicar|prepared by|analiticar|analitičar|аналитичар)\\s*[:\\-]?\\s*([^,\\n\\r]+)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern ORGANIZATION_PATTERN = Pattern.compile(
            "(?i)(organization|organizacija|agencija|agency|агенција|организација)\\s*[:\\-]?\\s*([^\\n\\r]+?)(?=\\s*(address|adresa|location|datum|$))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern ADDRESS_PATTERN = Pattern.compile(
            "(?i)(address|adresa|lokacija)\\s*[:\\-]\\s*([^\\n\\r]+?)(?=\\s*(?:Datum|Nivo|Date|Level|дату|Ниво|Vrijeme|$))",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
    );

    private static final Pattern MALWARE_PATTERN = Pattern.compile(
            "(?i)(malware|threat|virus|trojan|trojanac|malver|малвер|тројанац|тројан|pretnja|претња|naziv pretnje|назив претње|naziv virusa|нази вируса)\\s*[:\\-]?\\s*([A-Za-zА-Яа-я0-9\\.\\s\\-_]+?)(?=\\n|$)",
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
        String cleanText = pdfText.replace('\u00A0',' ').replaceAll("[\\r\\n]+","\n").replaceAll(" +"," ");
        return extractPatternValue(cleanText, ORGANIZATION_PATTERN);
    }

    public static String extractOrganizationAddress(String pdfText) {
        log.info("Extracting organization address from PDF text");
        try {

            String cleanText = pdfText.replace('\u00A0',' ').replaceAll("[\\r\\n]+","\n").replaceAll(" +"," ");

            log.debug("PDF Text (first 500 chars): {}",cleanText.substring(0,Math.min(500,cleanText.length())));

            Matcher matcher = ADDRESS_PATTERN.matcher(cleanText);

            if(matcher.find()) {
                String extracted = matcher.group(2).trim();
                log.info("Regex matched, raw extraction: {}",extracted);
                extracted = extracted.split("\\n")[0].trim(); // Uzmi samo prvi red vrednosti
                extracted = extracted.replaceAll("[:\\-]$", "").trim();

                log.info("Cleaned extraction: {}",extracted);

                if (extracted.length() > 3 && !extracted.equalsIgnoreCase("Unknown")) {
                    log.info("Found Address: {}", extracted);
                    return extracted;
                }
            }else{
                log.warn("Primary address pattern did not match");
            }

            log.warn("Primary address extraction failed, trying fallback patterns");
            Pattern simpleFallback = Pattern.compile("(?i)adresa[:\\-]\\s*(.+?)(?=\\n|$)",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

            Matcher fallbackMatcher = simpleFallback.matcher(cleanText);
            if(fallbackMatcher.find()) {
                String fallbackAddress = fallbackMatcher.group(1).trim();
                log.info("Fallback 1: Found address with simple pattern: {}",fallbackAddress);
                return fallbackAddress;
            }
            Pattern cityPattern = Pattern.compile(
                    "(?i)\\b(city|grad|mesto|град|место|place)\\s*[:\\-]?\\s*([A-Za-zА-Яа-я\\s]+?)(?=\\n|Datum|Nivo|Датум|Ниво|Date|Level$)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
            );


            String city = extractPatternValue(cleanText, cityPattern);
            if (city != null && !city.isEmpty() && !city.equals("Unknown")) {
                log.info("Fallback: Extracted city as address: {}", city);
                return city;
            }
        } catch (Exception e) {
            log.warn("Error extracting organization address: {}", e.getMessage());
        }
        log.warn("Could not extract organization address, returning Unknown");
        return "Unknown";
    }

    public static String extractMalware(String pdfText) {
        return extractPatternValue(pdfText, MALWARE_PATTERN);
    }

    public static String extractThreatLevel(String pdfText) {
        String extracted = extractPatternValue(pdfText, THREAT_LEVEL_PATTERN);
        return normalizeThreatLevel(extracted);
    }

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

    private static String extractPatternValue(String text, Pattern pattern) {
        if(text == null || text.isEmpty()) return "";
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(2);
            return value != null ? value.trim() : "";
        }
        return "";
    }

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
