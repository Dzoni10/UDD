package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.IndexDocumentDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.ParsedDocumentDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.exceptionhandling.exception.LoadingException;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexmodel.DummyIndex;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.indexrepository.DummyIndexRepository;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.DummyTable;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.repository.DummyRepository;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.FileService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.ParsePreviewService;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.util.PDFParserUtil;
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

    @Override
    public ParsedDocumentDTO parseDocument(String serverFilename) {
        log.info("Starting parse preview for: {}", serverFilename);

        try {
            // Učitaj PDF iz MinIO
            var minioResponse = fileService.loadAsResource(serverFilename);
            String pdfText = PDFParserUtil.extractTextFromPDF(minioResponse);

            // Ekstraktuj podatke
            String forensician = PDFParserUtil.extractForensician(pdfText);
            String organization = PDFParserUtil.extractOrganization(pdfText);
            String malwareName = PDFParserUtil.extractMalware(pdfText);
            String threatLevel = PDFParserUtil.extractThreatLevel(pdfText);
            String md5 = PDFParserUtil.extractMD5(pdfText);
            String sha256 = PDFParserUtil.extractSHA256(pdfText);
            String description = PDFParserUtil.extractDescription(pdfText, malwareName);

            // Ekstraktuj naslov iz filename-a
            String title = serverFilename.split("\\.")[0];

            log.info("Parse preview completed for: {}", serverFilename);

            return new ParsedDocumentDTO(
                    serverFilename,
                    title,
                    forensician,
                    organization,
                    malwareName,
                    description,
                    threatLevel,
                    md5,
                    sha256,
                    0.75 // Confidence score - može se poboljšati
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
            // Kreiraj DummyTable (PostgreSQL)
            DummyTable dbEntity = new DummyTable();
            dbEntity.setServerFilename(dto.serverFilename());
            dbEntity.setTitle(dto.serverFilename().split("\\.")[0]);
            dbEntity.setMimeType("application/pdf");
            dbEntity.setContent(dto.malwareDescription());

            DummyTable savedEntity = dummyRepository.save(dbEntity);

            // Kreiraj DummyIndex (Elasticsearch)
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

            // Dodaj vektorizaciju
            try {
                String textToVectorize = dto.malwareName() + " " + dto.malwareDescription();
                esEntity.setVectorizedContent(
                        VectorizationUtil.getEmbedding(textToVectorize)
                );
            } catch (Exception e) {
                log.warn("Could not vectorize document: {}", e.getMessage());
            }
            dummyIndexRepository.save(esEntity);
            log.info("Document indexed successfully: {}", dto.serverFilename());

        } catch (Exception e) {
            log.error("Error indexing document: {}", e.getMessage());
            throw new LoadingException("Error indexing document: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteDocument(String serverFilename) {
        log.info("Deleting document: {}", serverFilename);

        try {
            // Obriši iz MinIO
            fileService.delete(serverFilename);

            // Obriši iz PostgreSQL
            dummyRepository.deleteByServerFilename(serverFilename);

            log.info("Document deleted successfully: {}", serverFilename);

        } catch (Exception e) {
            log.error("Error deleting document: {}", e.getMessage());
            throw new LoadingException("Error deleting document: " + e.getMessage());
        }
    }
}
