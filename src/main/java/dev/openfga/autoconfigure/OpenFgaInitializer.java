package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.client.JsonSerializer;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.client.model.ClientRelationshipCondition;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientTupleKeyWithoutCondition;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.configuration.ClientReadOptions;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.ConsistencyPreference;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.jspecify.annotations.NonNull;
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
    private final JsonSerializer jsonSerializer;

    /**
     * Create a new initializer.
     *
     * @param fgaClient the {@link OpenFgaClient} to write the model and tuples with
     * @param initialization the initialization properties
     * @param resourceLoader the {@link ResourceLoader} used to resolve the configured locations
     * @param jsonSerializer the {@link JsonSerializer} used to deserialize the model and tuples
     */
    public OpenFgaInitializer(
            OpenFgaClient fgaClient,
            OpenFgaProperties.Initialization initialization,
            ResourceLoader resourceLoader,
            JsonSerializer jsonSerializer) {
        this.fgaClient = fgaClient;
        this.initialization = initialization;
        this.resourceLoader = resourceLoader;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        try {
            String authorizationModelId = findOrWriteAuthorizationModel();
            writeTuples(authorizationModelId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private String findOrWriteAuthorizationModel()
            throws FgaInvalidParameterException, IOException, ExecutionException, InterruptedException {
        var authorizationModel = fgaClient.readLatestAuthorizationModel().get().getAuthorizationModel();
        if (authorizationModel != null) {
            logger.info("OpenFGA store already has an authorization model; skipping model initialization");
            return authorizationModel.getId();
        }

        return writeModel();
    }

    private String writeModel()
            throws IOException, FgaInvalidParameterException, ExecutionException, InterruptedException {
        Resource resource = resourceLoader.getResource(initialization.getModelLocation());
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "OpenFGA authorization model location does not exist: " + initialization.getModelLocation());
        }

        var request = jsonSerializer.readValue(resource.getContentAsByteArray(), WriteAuthorizationModelRequest.class);
        String authorizationModelId =
                fgaClient.writeAuthorizationModel(request).get().getAuthorizationModelId();

        logger.info(
                "Wrote OpenFGA authorization model {} from {}",
                authorizationModelId,
                initialization.getModelLocation());
        return authorizationModelId;
    }

    private void writeTuples(String authorizationModelId)
            throws IOException, FgaInvalidParameterException, ExecutionException, InterruptedException {
        if (!StringUtils.hasText(initialization.getTuplesLocation())) {
            return;
        }

        Resource resource = resourceLoader.getResource(initialization.getTuplesLocation());
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "OpenFGA initial tuples location does not exist: " + initialization.getTuplesLocation());
        }

        var requestedTuples = jsonSerializer
                .readValue(resource.getContentAsByteArray(), InitialTuples.class)
                .toClientWriteRequest();
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

    private ClientWriteRequest pendingTuples(ClientWriteRequest requestedTuples)
            throws FgaInvalidParameterException, ExecutionException, InterruptedException {
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

    private boolean tupleExists(ClientTupleKey tuple)
            throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return readTuple(tuple).getTuples().stream()
                .anyMatch(existingTuple -> tuple.asTupleKey().equals(existingTuple.getKey()));
    }

    private boolean tupleExists(ClientTupleKeyWithoutCondition tuple)
            throws FgaInvalidParameterException, ExecutionException, InterruptedException {
        return !readTuple(tuple).getTuples().isEmpty();
    }

    private ClientReadResponse readTuple(ClientTupleKeyWithoutCondition tuple)
            throws FgaInvalidParameterException, ExecutionException, InterruptedException {
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

    /**
     * Represents the initial set of write and delete tuple operations required for initializing an OpenFGA store.
     * <p>
     * Holds two lists: one for tuples to be written ({@link InitialTupleKey}) and one for tuples to be deleted
     * ({@link InitialTupleKeyWithoutCondition}). Provides a method to convert this container into a {@link ClientWriteRequest}
     * suitable for sending to the OpenFGA client.
     * <p>
     * It is an internal helper class needed to replace the functionality that has previously been
     * provided by the Jackson mixins.
     */
    record InitialTuples(List<InitialTupleKey> writes, List<InitialTupleKeyWithoutCondition> deletes) {
        ClientWriteRequest toClientWriteRequest() {
            var request = new ClientWriteRequest();

            if (writes != null) {
                request.writes(
                        writes.stream().map(InitialTupleKey::toClientTupleKey).toList());
            }

            if (deletes != null) {
                request.deletes(deletes.stream()
                        .map(InitialTupleKeyWithoutCondition::toClientTupleKeyWithoutCondition)
                        .toList());
            }

            logger.trace("Initial load request: {}", request);
            return request;
        }
    }

    /**
     * Represents a tuple key used for initial authorization data, encapsulating the user, relation, object, and optional condition.
     * This record provides a mechanism to convert itself into a {@link ClientTupleKey} for interaction with the OpenFGA client.
     * <p>
     * This internal class is necessary to overcome the flaw of mapping <code>object</code> json properties into Java properties of name <code>_object</code>.
     */
    record InitialTupleKey(String user, String relation, String object, ClientRelationshipCondition condition) {
        ClientTupleKey toClientTupleKey() {
            var key = new ClientTupleKey().user(user).relation(relation)._object(object);

            if (condition != null) {
                key.condition(condition);
            }

            return key;
        }
    }

    /**
     * Represents an initial tuple key without a condition, used during authorization model initialization.
     * <p>
     * This record serves as an intermediate data structure to hold the essential parts of a tuple:
     * user, relation, and object. It provides conversion capability to a {@link ClientTupleKeyWithoutCondition}
     * for submission to the OpenFGA client during tuple writes.
     * <p>
     * This internal class is necessary to overcome the flaw of mapping <code>object</code> json properties into Java properties of name <code>_object</code>.
     */
    record InitialTupleKeyWithoutCondition(String user, String relation, String object) {
        ClientTupleKeyWithoutCondition toClientTupleKeyWithoutCondition() {
            return new ClientTupleKeyWithoutCondition()
                    .user(user)
                    .relation(relation)
                    ._object(object);
        }
    }
}
