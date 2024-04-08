package dev.openfga.example.config;

import dev.openfga.example.model.Document;
import dev.openfga.example.service.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Preloads database with two documents.
 */
@Configuration
public class LoadDocuments {
    private static final Logger log = LoggerFactory.getLogger(LoadDocuments.class);

    @Bean
    CommandLineRunner initDatabase(DocumentRepository repository) {

        return args -> {
            log.info("Preloading " + repository.save(new Document("1", "this is document 1 content")));
            log.info("Preloading " + repository.save(new Document("2", "this is document 2 content")));
        };
    }
}
