package dev.openfga.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.CreateStoreRequest;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Creates an FGA store, writes a simple authorization model, and adds a single tuple of form:
 * user: user:anne
 * relation: viewer
 * object: document:1
 *
 * This is for sample purposes only; would not be necessary in a real application.
 */
@Configuration
public class LoadFgaData implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(LoadFgaData.class);

    @Autowired
    private OpenFgaClient openFgaClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        loadFgaData();
    }

    private void loadFgaData() throws Exception {
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
