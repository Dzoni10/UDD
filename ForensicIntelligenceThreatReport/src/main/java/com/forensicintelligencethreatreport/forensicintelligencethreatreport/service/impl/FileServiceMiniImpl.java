package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.exceptionhandling.exception.NotFoundException;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.exceptionhandling.exception.StorageException;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.interfaces.FileService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileServiceMiniImpl implements FileService {

    private final MinioClient minioClient;

    @Value("${spring.minio.bucket}")
    private String bucketName;

    @Override
    public String store(MultipartFile file, String serverFilename) {
        if (file.isEmpty()) {
            log.error("Attempt to store empt file");
            throw new StorageException("Failed to store empty file.");
        }

        var originalFilenameTokens =
                Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
        var extension = originalFilenameTokens[originalFilenameTokens.length - 1];
        var finalFilename = serverFilename +"." +extension;

        try {

            boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if(!isBucketExists) {
                log.info("Bucket doesn't exist {}",bucketName);
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(serverFilename + "." + extension)
                    .headers(Collections.singletonMap("Content-Disposition",
                            "attachment; filename=\"" + file.getOriginalFilename() + "\""))
                    .stream(file.getInputStream(), file.getInputStream().available(), -1)
                    .build();
            minioClient.putObject(args);
            log.info("File stored successfully {} in bucket {}",finalFilename,bucketName);
        } catch (Exception e) {
            throw new StorageException("Error while storing file in Minio.");
        }

        return serverFilename + "." + extension;
    }

    @Override
    public void delete(String serverFilename) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(serverFilename)
                    .build();
            minioClient.removeObject(args);
            log.info("File deleted successfully: {}", serverFilename);
        } catch (Exception e) {
            log.error("Error while deleting file from Minio: {}", e.getMessage());
            throw new StorageException("Error while deleting " + serverFilename + " from Minio.");
        }
    }

    @Override
    public GetObjectResponse loadAsResource(String serverFilename) {
        try {
            var args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(serverFilename)
                    .build();

            log.info("Loading file from Minio: {}", serverFilename);
            return minioClient.getObject(args);
        } catch (Exception e) {
            log.error("Document not found in Minio: {}", serverFilename);
            throw new NotFoundException("Document " + serverFilename + " does not exist.");
        }
    }

    public String getSignedUrl(String serverFilename, int expirySeconds) {
        try {
            var args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(serverFilename)
                    .expiry(expirySeconds)
                    .build();

            String url = minioClient.getPresignedObjectUrl(args);
            log.info("Generated signed URL for: {}", serverFilename);
            return url;

        } catch (Exception e) {
            log.error("Error generating signed URL: {}", e.getMessage());
            throw new StorageException("Error generating signed URL: " + e.getMessage());
        }
    }

}

