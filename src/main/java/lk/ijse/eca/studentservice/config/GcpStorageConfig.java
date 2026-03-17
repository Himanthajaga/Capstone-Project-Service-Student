package lk.ijse.eca.studentservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@Slf4j
public class GcpStorageConfig {

    // Optional: only used in local dev (classpath key file)
    @Value("${gcp.credentials.location:#{null}}")
    private Resource credentialsFile;

    @Bean
    public Storage storage() throws IOException {

        // Cloud VM: use VM-attached service account (ADC) — no key file needed
        if (credentialsFile == null || !credentialsFile.exists()) {
            log.info("GCP Storage: using Application Default Credentials (VM service account)");
            return StorageOptions.getDefaultInstance().getService();
        }

        // Local dev fallback: use classpath key file
        log.info("GCP Storage: using credentials from {}", credentialsFile);
        GoogleCredentials credentials =
                ServiceAccountCredentials.fromStream(credentialsFile.getInputStream());

        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .build()
                .getService();
    }
}
