package dev.openfga.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.UnitTest;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadAuthorizationModelResponse;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteAuthorizationModelResponse;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.client.model.ClientWriteResponse;
import dev.openfga.sdk.api.configuration.ClientReadOptions;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.AuthorizationModel;
import dev.openfga.sdk.api.model.ConsistencyPreference;
import dev.openfga.sdk.api.model.Tuple;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@UnitTest
class OpenFgaInitializerTest {

    @Mock
    private OpenFgaClient fgaClient;

    @Mock
    private ResourceLoader resourceLoader;

    @Mock
    private ObjectMapper objectMapper;

    private OpenFgaProperties.Initialization initialization(String modelLocation, String tuplesLocation) {
        var initialization = new OpenFgaProperties.Initialization();
        initialization.setMode(OpenFgaProperties.InitializationMode.EMBEDDED);
        initialization.setModelLocation(modelLocation);
        initialization.setTuplesLocation(tuplesLocation);
        return initialization;
    }

    private OpenFgaInitializer initializer(String modelLocation, String tuplesLocation) {
        when(objectMapper.copy()).thenReturn(objectMapper);
        when(objectMapper.addMixIn(any(), any())).thenReturn(objectMapper);
        return new OpenFgaInitializer(
                fgaClient, initialization(modelLocation, tuplesLocation), resourceLoader, objectMapper);
    }

    private void stubLatestModel(AuthorizationModel model) throws Exception {
        var response = mock(ClientReadAuthorizationModelResponse.class);
        when(response.getAuthorizationModel()).thenReturn(model);
        when(fgaClient.readLatestAuthorizationModel()).thenReturn(CompletableFuture.completedFuture(response));
    }

    private void stubResource(String location, byte[] content) throws Exception {
        var resource = mock(Resource.class);
        when(resource.exists()).thenReturn(true);
        when(resource.getContentAsByteArray()).thenReturn(content);
        when(resourceLoader.getResource(location)).thenReturn(resource);
    }

    private ClientReadResponse tupleReadResponse(List<Tuple> tuples) {
        var response = mock(ClientReadResponse.class);
        when(response.getTuples()).thenReturn(tuples);
        return response;
    }

    private void stubTupleRead(List<Tuple> tuples) throws Exception {
        var response = tupleReadResponse(tuples);
        when(fgaClient.read(any(ClientReadRequest.class), any(ClientReadOptions.class)))
                .thenReturn(CompletableFuture.completedFuture(response));
    }

    private ClientWriteRequest tupleRequest() {
        return ClientWriteRequest.ofWrites(
                List.of(new ClientTupleKey().user("user:123").relation("reader")._object("document:1")));
    }

    private void stubModelWrite() throws Exception {
        var response = mock(ClientWriteAuthorizationModelResponse.class);
        when(response.getAuthorizationModelId()).thenReturn("01H...");
        when(fgaClient.writeAuthorizationModel(any())).thenReturn(CompletableFuture.completedFuture(response));
    }

    private static void assertInterruptRestored(Executable action) {
        try {
            Thread.currentThread().interrupt();
            assertThrows(InterruptedException.class, action);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void skipsWhenAuthorizationModelAlreadyExists() throws Exception {
        stubLatestModel(new AuthorizationModel());

        initializer("classpath:fga/model.json", null).run(null);

        verify(fgaClient, never()).writeAuthorizationModel(any());
        verify(fgaClient, never()).write(any(), any());
    }

    @Test
    void writesModelWhenNoneExists() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        when(objectMapper.readValue("{}".getBytes(), WriteAuthorizationModelRequest.class))
                .thenReturn(new WriteAuthorizationModelRequest());
        stubModelWrite();

        initializer("classpath:fga/model.json", null).run(null);

        verify(fgaClient).writeAuthorizationModel(any());
        verify(fgaClient, never()).write(any(), any());
    }

    @Test
    void writesModelAndTuplesTransactionallyWhenTuplesLocationConfigured() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        var tuples = "{\"writes\":[]}".getBytes();
        stubResource("classpath:fga/tuples.json", tuples);
        when(objectMapper.readValue("{}".getBytes(), WriteAuthorizationModelRequest.class))
                .thenReturn(new WriteAuthorizationModelRequest());
        when(objectMapper.readValue(tuples, ClientWriteRequest.class)).thenReturn(tupleRequest());
        stubTupleRead(List.of());
        stubModelWrite();
        when(fgaClient.write(any(), any(ClientWriteOptions.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(ClientWriteResponse.class)));

        initializer("classpath:fga/model.json", "classpath:fga/tuples.json").run(null);

        verify(fgaClient).writeAuthorizationModel(any());
        var writeOptionsCaptor = ArgumentCaptor.forClass(ClientWriteOptions.class);
        verify(fgaClient).write(any(), writeOptionsCaptor.capture());
        assertEquals("01H...", writeOptionsCaptor.getValue().getAuthorizationModelId());
        assertTrue(writeOptionsCaptor.getValue().isTransactionsEnabled());
        var readOptionsCaptor = ArgumentCaptor.forClass(ClientReadOptions.class);
        verify(fgaClient).read(any(), readOptionsCaptor.capture());
        assertEquals(
                ConsistencyPreference.HIGHER_CONSISTENCY,
                readOptionsCaptor.getValue().getConsistency());
    }

    @Test
    void deserializesClasspathInitializationFixtures() throws Exception {
        stubLatestModel(null);
        stubTupleRead(List.of());
        stubModelWrite();
        when(fgaClient.write(any(), any(ClientWriteOptions.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(ClientWriteResponse.class)));
        var initializer = new OpenFgaInitializer(
                fgaClient,
                initialization("classpath:fga/model.json", "classpath:fga/tuples.json"),
                new DefaultResourceLoader(),
                new ObjectMapper());

        initializer.run(null);

        var modelRequestCaptor = ArgumentCaptor.forClass(WriteAuthorizationModelRequest.class);
        verify(fgaClient).writeAuthorizationModel(modelRequestCaptor.capture());
        assertEquals("1.1", modelRequestCaptor.getValue().getSchemaVersion());
        assertEquals(2, modelRequestCaptor.getValue().getTypeDefinitions().size());
        var tupleRequestCaptor = ArgumentCaptor.forClass(ClientWriteRequest.class);
        verify(fgaClient).write(tupleRequestCaptor.capture(), any(ClientWriteOptions.class));
        assertEquals(1, tupleRequestCaptor.getValue().getWrites().size());
        assertEquals(
                "user:123", tupleRequestCaptor.getValue().getWrites().get(0).getUser());
        assertEquals("reader", tupleRequestCaptor.getValue().getWrites().get(0).getRelation());
        assertEquals(
                "document:1", tupleRequestCaptor.getValue().getWrites().get(0).getObject());
    }

    @Test
    void retriesTuplesAgainstExistingModelAfterPreviousFailure() throws Exception {
        var noModelResponse = mock(ClientReadAuthorizationModelResponse.class);
        var existingModelResponse = mock(ClientReadAuthorizationModelResponse.class);
        when(existingModelResponse.getAuthorizationModel()).thenReturn(new AuthorizationModel().id("01H..."));
        when(fgaClient.readLatestAuthorizationModel())
                .thenReturn(
                        CompletableFuture.completedFuture(noModelResponse),
                        CompletableFuture.completedFuture(existingModelResponse));
        stubResource("classpath:fga/model.json", "{}".getBytes());
        var tuples = "{\"writes\":[]}".getBytes();
        stubResource("classpath:fga/tuples.json", tuples);
        when(objectMapper.readValue("{}".getBytes(), WriteAuthorizationModelRequest.class))
                .thenReturn(new WriteAuthorizationModelRequest());
        when(objectMapper.readValue(tuples, ClientWriteRequest.class)).thenReturn(tupleRequest());
        stubTupleRead(List.of());
        stubModelWrite();
        var failedWrite = new CompletableFuture<ClientWriteResponse>();
        failedWrite.completeExceptionally(new IllegalStateException("tuple write failed"));
        when(fgaClient.write(any(), any(ClientWriteOptions.class)))
                .thenReturn(failedWrite, CompletableFuture.completedFuture(mock(ClientWriteResponse.class)));
        var initializer = initializer("classpath:fga/model.json", "classpath:fga/tuples.json");

        assertThrows(ExecutionException.class, () -> initializer.run(null));
        initializer.run(null);

        verify(fgaClient).writeAuthorizationModel(any());
        verify(fgaClient, times(2)).write(any(), any(ClientWriteOptions.class));
    }

    @Test
    void acceptsConcurrentTupleInitializationWhenDesiredStateExistsAfterFailure() throws Exception {
        stubLatestModel(new AuthorizationModel().id("01H..."));
        var tuples = "{\"writes\":[]}".getBytes();
        stubResource("classpath:fga/tuples.json", tuples);
        var tupleRequest = tupleRequest();
        when(objectMapper.readValue(tuples, ClientWriteRequest.class)).thenReturn(tupleRequest);
        var missingTuples = tupleReadResponse(List.of());
        var existingTuple = new Tuple().key(tupleRequest.getWrites().get(0).asTupleKey());
        var existingTuples = tupleReadResponse(List.of(existingTuple));
        when(fgaClient.read(any(ClientReadRequest.class), any(ClientReadOptions.class)))
                .thenReturn(
                        CompletableFuture.completedFuture(missingTuples),
                        CompletableFuture.completedFuture(existingTuples));
        var failedWrite = new CompletableFuture<ClientWriteResponse>();
        failedWrite.completeExceptionally(new IllegalStateException("duplicate tuple"));
        when(fgaClient.write(any(), any(ClientWriteOptions.class))).thenReturn(failedWrite);

        initializer("classpath:fga/model.json", "classpath:fga/tuples.json").run(null);

        verify(fgaClient, never()).writeAuthorizationModel(any());
        verify(fgaClient).write(any(), any(ClientWriteOptions.class));
    }

    @Test
    void propagatesFailureFromModelReadInsteadOfWriting() throws Exception {
        var failed = new CompletableFuture<ClientReadAuthorizationModelResponse>();
        failed.completeExceptionally(new IllegalStateException("403 Forbidden"));
        when(fgaClient.readLatestAuthorizationModel()).thenReturn(failed);

        assertThrows(ExecutionException.class, () -> initializer("classpath:fga/model.json", null)
                .run(null));

        verify(fgaClient, never()).writeAuthorizationModel(any());
    }

    @Test
    void restoresInterruptFlagWhenModelReadIsInterrupted() throws Exception {
        when(fgaClient.readLatestAuthorizationModel()).thenReturn(new CompletableFuture<>());

        assertInterruptRestored(
                () -> initializer("classpath:fga/model.json", null).run(null));
    }

    @Test
    void restoresInterruptFlagWhenModelWriteIsInterrupted() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        when(objectMapper.readValue("{}".getBytes(), WriteAuthorizationModelRequest.class))
                .thenReturn(new WriteAuthorizationModelRequest());
        when(fgaClient.writeAuthorizationModel(any())).thenReturn(new CompletableFuture<>());

        assertInterruptRestored(
                () -> initializer("classpath:fga/model.json", null).run(null));
    }

    @Test
    void restoresInterruptFlagWhenTupleWriteIsInterrupted() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        var tuples = "{\"writes\":[]}".getBytes();
        stubResource("classpath:fga/tuples.json", tuples);
        when(objectMapper.readValue("{}".getBytes(), WriteAuthorizationModelRequest.class))
                .thenReturn(new WriteAuthorizationModelRequest());
        when(objectMapper.readValue(tuples, ClientWriteRequest.class)).thenReturn(tupleRequest());
        stubTupleRead(List.of());
        stubModelWrite();
        when(fgaClient.write(any(), any(ClientWriteOptions.class))).thenReturn(new CompletableFuture<>());

        assertInterruptRestored(() -> initializer("classpath:fga/model.json", "classpath:fga/tuples.json")
                .run(null));
    }

    @Test
    void failsWhenModelLocationDoesNotExist() throws Exception {
        stubLatestModel(null);
        var resource = mock(Resource.class);
        when(resource.exists()).thenReturn(false);
        when(resourceLoader.getResource("classpath:fga/missing.json")).thenReturn(resource);

        assertThrows(IllegalStateException.class, () -> initializer("classpath:fga/missing.json", null)
                .run(null));

        verify(fgaClient, never()).writeAuthorizationModel(any());
    }
}
