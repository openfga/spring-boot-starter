package dev.openfga.autoconfigure;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientReadOptions;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.ConsistencyPreference;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
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
 * <p>The initializer writes the configured model only when the store has none. If tuples are
 * configured, it transactionally ensures them on every startup so interrupted initialization can
 * be retried safely. Any failure is propagated so that the application fails fast.
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
        this.objectMapper = objectMapper
                .copy()
                .addMixIn(ClientTupleKey.class, ClientTupleKeyMixin.class)
                .addMixIn(ClientTupleKeyWithoutCondition.class, ClientTupleKeyWithoutConditionMixin.class);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            String authorizationModelId = findOrWriteAuthorizationModel();
            writeTuples(authorizationModelId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private String findOrWriteAuthorizationModel() throws Exception {
        var authorizationModel = fgaClient.readLatestAuthorizationModel().get().getAuthorizationModel();
        if (authorizationModel != null) {
            logger.info("OpenFGA store already has an authorization model; skipping model initialization");
            return authorizationModel.getId();
        }
        return writeModel();
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
        var requestedTuples = objectMapper.readValue(resource.getContentAsByteArray(), ClientWriteRequest.class);
        var pendingTuples = pendingTuples(requestedTuples);
        if (!hasChanges(pendingTuples)) {
            logger.info("Initial OpenFGA tuples already present; skipping tuple initialization");
            return;
        }
        try {
            fgaClient
                    .write(pendingTuples, new ClientWriteOptions().authorizationModelId(authorizationModelId))
                    .get();
        } catch (ExecutionException e) {
            if (hasChanges(pendingTuples(requestedTuples))) {
                throw e;
            }
        }
        logger.info("Wrote initial OpenFGA tuples from {}", initialization.getTuplesLocation());
    }

    private ClientWriteRequest pendingTuples(ClientWriteRequest requestedTuples) throws Exception {
        var pendingTuples = new ClientWriteRequest();
        if (requestedTuples.getWrites() != null) {
            var pendingWrites = new ArrayList<ClientTupleKey>();
            for (var tuple : requestedTuples.getWrites()) {
                if (!tupleExists(tuple)) {
                    pendingWrites.add(tuple);
                }
            }
            pendingTuples.writes(pendingWrites);
        }
        if (requestedTuples.getDeletes() != null) {
            var pendingDeletes = new ArrayList<ClientTupleKeyWithoutCondition>();
            for (var tuple : requestedTuples.getDeletes()) {
                if (tupleExists(tuple)) {
                    pendingDeletes.add(tuple);
                }
            }
            pendingTuples.deletes(pendingDeletes);
        }
        return pendingTuples;
    }

    private boolean tupleExists(ClientTupleKey tuple) throws Exception {
        return readTuple(tuple).getTuples().stream()
                .anyMatch(existingTuple -> tuple.asTupleKey().equals(existingTuple.getKey()));
    }

    private boolean tupleExists(ClientTupleKeyWithoutCondition tuple) throws Exception {
        return !readTuple(tuple).getTuples().isEmpty();
    }

    private ClientReadResponse readTuple(ClientTupleKeyWithoutCondition tuple) throws Exception {
        var request = new ClientReadRequest()
                .user(tuple.getUser())
                .relation(tuple.getRelation())
                ._object(tuple.getObject());
        var options = new ClientReadOptions().consistency(ConsistencyPreference.HIGHER_CONSISTENCY);
        return fgaClient.read(request, options).get();
    }

    private static boolean hasChanges(ClientWriteRequest request) {
        return (request.getWrites() != null && !request.getWrites().isEmpty())
                || (request.getDeletes() != null && !request.getDeletes().isEmpty());
    }

    private abstract static class ClientTupleKeyMixin {
        @JsonSetter("object")
        abstract ClientTupleKey _object(String object);
    }

    private abstract static class ClientTupleKeyWithoutConditionMixin {
        @JsonSetter("object")
        abstract ClientTupleKeyWithoutCondition _object(String object);
    }
}
