package com.example.crafteria_server.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class EnvBean {
    @Value("${spring.cloud.gcp.storage.credentials.location}")
    private String credentialsLocation;

    @Value("${spring.cloud.gcp.storage.project-id}")
    private String projectId;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    private String bucketName;

    @Value("${spring.cloud.gcp.storage.key-name}")
    private String keyName;
}
