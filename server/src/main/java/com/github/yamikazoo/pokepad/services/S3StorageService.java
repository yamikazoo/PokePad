package com.github.yamikazoo.pokepad.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private static final String VALID_KEY_PATTERN = "^[a-zA-Z0-9._/-]+$";

    private final S3Client s3Client;
    private final String region;
    private final String bucket;

    public S3StorageService(
            S3Client s3Client,
            @Value("${aws.region}") String region,
            @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.region = region;
        this.bucket = bucket;
    }

    public String getBucket() {
        return bucket;
    }

    public void uploadCatalogImage(String key, byte[] bytes, String contentType) {
        String sanitizedKey = sanitizeKey(key);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(sanitizedKey)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    public String publicUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + sanitizeKey(key);
    }

    public String sanitizeKey(String key) {
        if (key == null || key.isBlank() || key.contains("..") || !key.matches(VALID_KEY_PATTERN)) {
            throw new IllegalArgumentException("Invalid S3 object key: " + key);
        }
        return key;
    }
}
