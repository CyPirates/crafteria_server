package com.example.crafteria_server.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class BeanConfig {
    private final EnvBean envBean;

    @Bean
    public Storage storage() throws IOException {
        ClassPathResource resource = new ClassPathResource(envBean.getKeyName());
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());
        String projectId = envBean.getProjectId();

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
