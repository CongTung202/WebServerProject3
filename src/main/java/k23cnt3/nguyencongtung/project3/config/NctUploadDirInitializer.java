package k23cnt3.nguyencongtung.project3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class NctUploadDirInitializer {

    @Value("${nct.upload.dir:./uploads}")
    private String nctUploadDirectory;

    @PostConstruct
    public void nctInitUploadDirectory() {
        try {
            Files.createDirectories(Paths.get(nctUploadDirectory));
            System.out.println("Upload directory created: " + nctUploadDirectory);
        } catch (IOException e) {
            System.err.println("Could not create upload directory: " + e.getMessage());
        }
    }
}