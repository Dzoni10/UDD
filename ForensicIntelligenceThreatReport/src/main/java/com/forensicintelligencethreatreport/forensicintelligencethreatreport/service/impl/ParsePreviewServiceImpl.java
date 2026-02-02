package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.GeocodeResponseDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.IndexDocumentDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.ParsedDocumentDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.exceptionhandling.exception.LoadingException;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexrepository.DummyIndexRepository;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.DummyTable;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.repository.DummyRepository;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.FileService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.GeoLocationService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.ParsePreviewService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.PDFParserUtil;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.StatisticLogger;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.VectorizationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParsePreviewServiceImpl implements ParsePreviewService {

    private final FileService fileService;
    private final DummyRepository dummyRepository;
    private final DummyIndexRepository dummyIndexRepository;
    private final GeoLocationService geoLocationService;
    private final StatisticLogger statisticsLogger;


    @Override
    public ParsedDocumentDTO parseDocument(String serverFilename) {
        log.info("Starting parse preview for: {}", serverFilename);

        try {
            var minioResponse = fileService.loadAsResource(serverFilename);
            String pdfText = PDFParserUtil.extractTextFromPDF(minioResponse);
            String forensician = PDFParserUtil.extractForensician(pdfText);
            String organization = PDFParserUtil.extractOrganization(pdfText);
            String organizationAddress = PDFParserUtil.extractOrganizationAddress(pdfText);
            String malwareName = PDFParserUtil.extractMalware(pdfText);
            String threatLevel = PDFParserUtil.extractThreatLevel(pdfText);
            String md5 = PDFParserUtil.extractMD5(pdfText);
            String sha256 = PDFParserUtil.extractSHA256(pdfText);
            String description = PDFParserUtil.extractDescription(pdfText, malwareName);
            String title = serverFilename.split("\\.")[0];
            log.info("Parse preview completed for: {}", serverFilename);
            log.info("Extracted organization: {}, Address: {}", organization, organizationAddress);

            statisticsLogger.logPdfParsed(
                    serverFilename,
                    forensician,
                    organization,
                    organizationAddress,
                    malwareName,
                    threatLevel
            );

            return new ParsedDocumentDTO(
                    serverFilename,
                    title,
                    forensician,
                    organization,
                    organizationAddress,
                    malwareName,
                    description,
                    threatLevel,
                    md5,
                    sha256,
                    0.85
            );
        } catch (Exception e) {
            log.error("Error parsing document: {}", e.getMessage());
            throw new LoadingException("Error parsing document: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void indexDocument(IndexDocumentDTO dto) {
        log.info("Indexing document: {}", dto.serverFilename());

        try {
            DummyTable dbEntity = new DummyTable();
            dbEntity.setServerFilename(dto.serverFilename());
            dbEntity.setTitle(dto.serverFilename().split("\\.")[0]);
            dbEntity.setMimeType("application/pdf");
            dbEntity.setContent(dto.malwareDescription());
            DummyTable savedEntity = dummyRepository.save(dbEntity);

            DummyIndex esEntity = new DummyIndex();
            esEntity.setId(UUID.randomUUID().toString());
            esEntity.setDatabaseId(savedEntity.getId());
            esEntity.setServerFilename(dto.serverFilename());
            esEntity.setTitle(dto.serverFilename().split("\\.")[0]);
            esEntity.setContentSr(dto.malwareDescription());
            esEntity.setForensicianName(dto.forensicianName());
            esEntity.setOrganization(dto.organization());
            esEntity.setMalwareName(dto.malwareName());
            esEntity.setThreatLevel(dto.threatLevel());
            esEntity.setHashMd5(dto.hashMD5());
            esEntity.setHashSha256(dto.hashSHA256());

            try {
                String addressToGeocode = null;
                if (dto.organizationAddress() != null && !dto.organizationAddress().isBlank() && !dto.organizationAddress().equalsIgnoreCase("Unknown")) {
                    log.info("Geocoding organization address: {}", dto.organizationAddress());
                    addressToGeocode = dto.organizationAddress();
                    log.info("Using extracted address for geocoding:{}", addressToGeocode);
                } else if (dto.organization() != null && !dto.organization().isBlank()) {
                    addressToGeocode = dto.organization() + ", Serbia";
                    log.warn("Address unknown, falling back to organization name: {}", addressToGeocode);
                }
                if (addressToGeocode != null) {
                    addressToGeocode = addressToGeocode.replaceAll("[()]","").trim();
                    GeocodeResponseDTO geocodeResponse = geoLocationService.geocodeAddress(addressToGeocode);

                    esEntity.setOrganizationCity(geocodeResponse.city());
                    esEntity.setOrganizationCountry(geocodeResponse.country());
                    esEntity.setOrganizationLatitude(geocodeResponse.latitude());
                    esEntity.setOrganizationLongitude(geocodeResponse.longitude());

                    log.info("Successfully geocoded {} -> Lat: {}, Lng: {}",
                            addressToGeocode, geocodeResponse.latitude(), geocodeResponse.longitude());

                    statisticsLogger.logGeocoding(
                            addressToGeocode,
                            geocodeResponse.city(),
                            geocodeResponse.latitude(),
                            geocodeResponse.longitude(),
                            geocodeResponse.country(),
                            true
                    );

                }else{
                    log.error("No valid address or organization name found for geocoding.");
                    setDefaultLocation(esEntity);

                    statisticsLogger.logGeocoding(
                            "Unknown",
                            "Unknown",
                            0.0,
                            0.0,
                            "Unknown",
                            false
                    );
            }
            } catch (Exception e) {
                log.warn("Geocoding failed for document {}: {}", dto.serverFilename(), e.getMessage());
                setDefaultLocation(esEntity);
                statisticsLogger.logIndexingError(dto.serverFilename(), e.getMessage());
            }

            try {
                String textToVectorize = dto.malwareName() + " " + dto.malwareDescription();
                esEntity.setVectorizedContent(
                        VectorizationUtil.getEmbedding(textToVectorize)
                );
                log.info("Document vectorized successfully");
            } catch (Exception e) {
                log.warn("Could not vectorize document: {}", e.getMessage());
            }
            dummyIndexRepository.save(esEntity);
            log.info("Document indexed successfully: {}", dto.serverFilename());

            statisticsLogger.logDocumentIndexed(
                    dto.serverFilename(),
                    dto.forensicianName(),
                    dto.organization(),
                    esEntity.getOrganizationCity(),
                    esEntity.getOrganizationCountry(),
                    dto.malwareName(),
                    dto.threatLevel(),
                    dto.hashMD5(),
                    dto.hashSHA256()
            );

        } catch (Exception e) {
            log.error("Error indexing document: {}", e.getMessage());
            throw new LoadingException("Error indexing document: " + e.getMessage());
        }
    }

    private void setDefaultLocation(DummyIndex esEntity) {
        esEntity.setOrganizationCity("Unknown");
        esEntity.setOrganizationCountry("Unknown");
        esEntity.setOrganizationLatitude(0.0);
        esEntity.setOrganizationLongitude(0.0);
    }

    @Override
    @Transactional
    public void deleteDocument(String serverFilename) {
        log.info("Deleting document: {}", serverFilename);
        try {
            fileService.delete(serverFilename);
            dummyRepository.deleteByServerFilename(serverFilename);
            log.info("Document deleted successfully: {}", serverFilename);
        } catch (Exception e) {
            log.error("Error deleting document: {}", e.getMessage());
            throw new LoadingException("Error deleting document: " + e.getMessage());
        }
    }
}
