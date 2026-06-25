package dev.openfga.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

/**
 * Writes an initial authorization model, and optionally a set of initial tuples, into OpenFGA at
 * application startup. This is the OpenFGA analogue of Spring Boot's {@code schema.sql}/{@code data.sql}
 * database initialization.
 *
 * <p>The initializer is idempotent: if the configured store already has an authorization model, it
 * does nothing. Otherwise it writes the model from {@code openfga.initialization.model-location} and,
 * if configured, the tuples from {@code openfga.initialization.tuples-location}. Any failure is
 * propagated so that the application fails fast.
 */
public class OpenFgaInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(OpenFgaInitializer.class);

    private final OpenFgaClient fgaClient;
    private final OpenFgaProperties.Initialization initialization;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    /**
     * Create a new initializer.
     *
     * @param fgaClient the {@link OpenFgaClient} to write the model and tuples with
     * @param initialization the initialization properties
     * @param resourceLoader the {@link ResourceLoader} used to resolve the configured locations
     * @param objectMapper the {@link ObjectMapper} used to deserialize the model and tuples
     */
    public OpenFgaInitializer(
            OpenFgaClient fgaClient,
            OpenFgaProperties.Initialization initialization,
            ResourceLoader resourceLoader,
            ObjectMapper objectMapper) {
        this.fgaClient = fgaClient;
        this.initialization = initialization;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (authorizationModelExists()) {
            logger.info("OpenFGA store already has an authorization model; skipping initialization");
            return;
        }
        String authorizationModelId = writeModel();
        writeTuples(authorizationModelId);
    }

    private boolean authorizationModelExists() throws Exception {
        return fgaClient.readLatestAuthorizationModel().get().getAuthorizationModel() != null;
    }

    private String writeModel() throws Exception {
        Resource resource = resourceLoader.getResource(initialization.getModelLocation());
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "OpenFGA authorization model location does not exist: " + initialization.getModelLocation());
        }
        var request = objectMapper.readValue(resource.getContentAsByteArray(), WriteAuthorizationModelRequest.class);
        String authorizationModelId =
                fgaClient.writeAuthorizationModel(request).get().getAuthorizationModelId();
        logger.info(
                "Wrote OpenFGA authorization model {} from {}",
                authorizationModelId,
                initialization.getModelLocation());
        return authorizationModelId;
    }

    private void writeTuples(String authorizationModelId) throws Exception {
        if (!StringUtils.hasText(initialization.getTuplesLocation())) {
            return;
        }
        Resource resource = resourceLoader.getResource(initialization.getTuplesLocation());
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "OpenFGA initial tuples location does not exist: " + initialization.getTuplesLocation());
        }
        var request = objectMapper.readValue(resource.getContentAsByteArray(), ClientWriteRequest.class);
        fgaClient
                .write(
                        request,
                        new ClientWriteOptions()
                                .authorizationModelId(authorizationModelId)
                                .disableTransactions(true))
                .get();
        logger.info("Wrote initial OpenFGA tuples from {}", initialization.getTuplesLocation());
    }
}
