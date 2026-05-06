package com.example.nsu_backend.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Slf4j
@Configuration
public class RustFsConfig {
    @Value("${rust-fs.uri}")
    String rustFsUri;

    @Bean
    public S3Client rustFsClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(rustFsUri)) // RustFS address
                .region(Region.US_EAST_1) // RustFS does not validate regions
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("admin", "123")
                        )
                )
                .forcePathStyle(true) // Required for RustFS compatibility
                .build();
    }
}
