package dev.openfga.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.example.Document;
import dev.openfga.example.DocumentService;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class LoadData {

    private static final Logger logger = LoggerFactory.getLogger(LoadData.class);

    @Bean
    CommandLineRunner initDatabase(OpenFgaClient openFgaClient, ObjectMapper objectMapper, DocumentService documentService) {
        return args -> {
            loadFgaData(openFgaClient, objectMapper);
            loadDocuments(documentService);
        };
    }

    private void loadDocuments(DocumentService documentService) {
        logger.info("Preloading " + documentService.saveDocument(new Document("1", "this is document 1 content")));
        logger.info("Preloading " + documentService.saveDocument(new Document("2", "this is document 2 content")));
    }

    private void loadFgaData(OpenFgaClient openFgaClient, ObjectMapper objectMapper) throws Exception {
        // CreateStore
        logger.debug("Creating Test Store");
        var store = openFgaClient
                .createStore(new CreateStoreRequest().name("Demo Store"))
                .get();
        logger.debug("Test Store ID: " + store.getId());

        // Set the store id
        openFgaClient.setStoreId(store.getId());

        // ListStores after Create
        logger.debug("Listing Stores");
        var stores = openFgaClient.listStores().get();
        logger.debug("Stores Count: " + stores.getStores().size());

        // GetStore
        logger.debug("Getting Current Store");
        var currentStore = openFgaClient.getStore().get();
        logger.debug("Current Store Name: " + currentStore.getName());

        var authModelJson = loadResource();
        var authorizationModel = openFgaClient
                .writeAuthorizationModel(objectMapper.readValue(authModelJson, WriteAuthorizationModelRequest.class))
                .get();
        logger.debug("Authorization Model ID " + authorizationModel.getAuthorizationModelId());

        // Set the model ID
        openFgaClient.setAuthorizationModelId(authorizationModel.getAuthorizationModelId());

        // Write
        logger.debug("Writing Tuples");
        openFgaClient
                .write(
                        new ClientWriteRequest()
                                .writes(List.of(new ClientTupleKey()
                                        .user("user:anne")
                                        .relation("viewer")
                                        ._object("document:1"))),
                        new ClientWriteOptions()
                                .disableTransactions(true)
                                .authorizationModelId(authorizationModel.getAuthorizationModelId()))
                .get();
        logger.debug("Done Writing Tuples");

        // Read
        logger.debug("Reading Tuples");
        var readTuples = openFgaClient.read(new ClientReadRequest()).get();
        logger.debug("Read Tuples" + objectMapper.writeValueAsString(readTuples));
    }

    private String loadResource() {
        try {
            return new ClassPathResource("example-auth-model.json").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to load resource: " + "example-auth-model.json", ioe);
        }
    }
}
