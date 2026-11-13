package dev.openfga.autoconfigure;

import static dev.openfga.autoconfigure.OpenFgaInitializer.InitialTupleKeyWithoutCondition;
import static dev.openfga.autoconfigure.OpenFgaInitializer.InitialTuples;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import dev.openfga.UnitTest;
import dev.openfga.autoconfigure.OpenFgaInitializer.InitialCondition;
import dev.openfga.autoconfigure.OpenFgaInitializer.InitialTupleKey;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadAuthorizationModelResponse;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.client.model.ClientWriteAuthorizationModelResponse;
import dev.openfga.sdk.api.client.model.ClientWriteResponse;
import dev.openfga.sdk.api.configuration.ClientReadOptions;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.AuthorizationModel;
import dev.openfga.sdk.api.model.ConsistencyPreference;
import dev.openfga.sdk.api.model.Tuple;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SuppressWarnings("ResultOfMethodCallIgnored")
@UnitTest
class OpenFgaInitializerTest {

    @Mock
    private OpenFgaClient fgaClient;

    @Mock
    private ResourceLoader resourceLoader;

    private OpenFgaProperties.Initialization initialization(String modelLocation, String tuplesLocation) {
        var initialization = new OpenFgaProperties.Initialization();
        initialization.setMode(OpenFgaProperties.InitializationMode.EMBEDDED);
        initialization.setModelLocation(modelLocation);
        initialization.setTuplesLocation(tuplesLocation);
        return initialization;
    }

    private OpenFgaInitializer initializer(String modelLocation, String tuplesLocation) {
        return new OpenFgaInitializer(fgaClient, initialization(modelLocation, tuplesLocation), resourceLoader);
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

    private InitialTuples initialTuples() {
        return new InitialTuples(List.of(new InitialTupleKey("user:123", "reader", "document:1", null)), null);
    }

    private byte[] tupleJson() {
        return "{\"writes\":[{\"user\":\"user:123\",\"relation\":\"reader\",\"object\":\"document:1\"}]}".getBytes();
    }

    private void stubModelWrite() throws Exception {
        var response = mock(ClientWriteAuthorizationModelResponse.class);
        when(response.getAuthorizationModelId()).thenReturn("01H...");
        when(fgaClient.writeAuthorizationModel(any())).thenReturn(CompletableFuture.completedFuture(response));
    }

    private List<String> captureTrace(Runnable action) {
        var logger = (Logger) LoggerFactory.getLogger(OpenFgaInitializer.class);
        var previousLevel = logger.getLevel();
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);
        try {
            action.run();
            return appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
            appender.stop();
        }
    }

    private static void assertInterruptRestored(Executable action) {
        try {
            currentThread().interrupt();
            assertThrows(InterruptedException.class, action);
            assertTrue(currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void skipsWhenAuthorizationModelAlreadyExists() throws Exception {
        stubLatestModel(new AuthorizationModel());

        initializer("classpath:fga/model.json", null).run(new DefaultApplicationArguments());

        verify(fgaClient, never()).writeAuthorizationModel(any());
        verify(fgaClient, never()).write(any(), any());
    }

    @Test
    void writesModelWhenNoneExists() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        stubModelWrite();

        initializer("classpath:fga/model.json", null).run(new DefaultApplicationArguments());

        verify(fgaClient).writeAuthorizationModel(any());
        verify(fgaClient, never()).write(any(), any());
    }

    @Test
    void writesModelAndTuplesTransactionallyWhenTuplesLocationConfigured() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        var tuples = tupleJson();
        stubResource("classpath:fga/tuples.json", tuples);
        stubTupleRead(List.of());
        stubModelWrite();
        when(fgaClient.write(any(), any(ClientWriteOptions.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(ClientWriteResponse.class)));

        initializer("classpath:fga/model.json", "classpath:fga/tuples.json").run(new DefaultApplicationArguments());

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
    void logsTupleFieldsAndConditionAtTraceLevel() {
        var condition = new InitialCondition("is_valid", Map.of("key", "value"));
        var messages = captureTrace(() -> new InitialTuples(
                        List.of(
                                new InitialTupleKey("user:123", "reader", "document:1", null),
                                new InitialTupleKey("user:456", "writer", "document:2", condition)),
                        List.of(new InitialTupleKeyWithoutCondition("user:789", "reader", "document:3")))
                .toClientWriteRequest());

        assertEquals(
                List.of("Initial load request: writes=[user=user:123, relation=reader, object=document:1, "
                        + "user=user:456, relation=writer, object=document:2, condition=is_valid, context={key=value}], "
                        + "deletes=[user=user:789, relation=reader, object=document:3]"),
                messages);
    }

    @Test
    void logsNullTupleListsAsEmptyLists() {
        var messages = captureTrace(() -> new InitialTuples(null, null).toClientWriteRequest());

        assertEquals(List.of("Initial load request: writes=[], deletes=[]"), messages);
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
        var tuples = tupleJson();
        stubResource("classpath:fga/tuples.json", tuples);
        stubTupleRead(List.of());
        stubModelWrite();
        var failedWrite = new CompletableFuture<ClientWriteResponse>();
        failedWrite.completeExceptionally(new IllegalStateException("tuple write failed"));
        when(fgaClient.write(any(), any(ClientWriteOptions.class)))
                .thenReturn(failedWrite, CompletableFuture.completedFuture(mock(ClientWriteResponse.class)));
        var initializer = initializer("classpath:fga/model.json", "classpath:fga/tuples.json");

        assertThrows(ExecutionException.class, () -> initializer.run(new DefaultApplicationArguments()));
        initializer.run(new DefaultApplicationArguments());

        verify(fgaClient).writeAuthorizationModel(any());
        verify(fgaClient, times(2)).write(any(), any(ClientWriteOptions.class));
    }

    @Test
    void acceptsConcurrentTupleInitializationWhenDesiredStateExistsAfterFailure() throws Exception {
        stubLatestModel(new AuthorizationModel().id("01H..."));
        var tuples = tupleJson();
        stubResource("classpath:fga/tuples.json", tuples);
        var initialTuples = initialTuples();
        var missingTuples = tupleReadResponse(List.of());
        var existingTuple = new Tuple()
                .key(initialTuples.toClientWriteRequest().getWrites().get(0).asTupleKey());
        var existingTuples = tupleReadResponse(List.of(existingTuple));
        when(fgaClient.read(any(ClientReadRequest.class), any(ClientReadOptions.class)))
                .thenReturn(
                        CompletableFuture.completedFuture(missingTuples),
                        CompletableFuture.completedFuture(existingTuples));
        var failedWrite = new CompletableFuture<ClientWriteResponse>();
        failedWrite.completeExceptionally(new IllegalStateException("duplicate tuple"));
        when(fgaClient.write(any(), any(ClientWriteOptions.class))).thenReturn(failedWrite);

        initializer("classpath:fga/model.json", "classpath:fga/tuples.json").run(new DefaultApplicationArguments());

        verify(fgaClient, never()).writeAuthorizationModel(any());
        verify(fgaClient).write(any(), any(ClientWriteOptions.class));
    }

    @Test
    void propagatesFailureFromModelReadInsteadOfWriting() throws Exception {
        var failed = new CompletableFuture<ClientReadAuthorizationModelResponse>();
        failed.completeExceptionally(new IllegalStateException("403 Forbidden"));
        when(fgaClient.readLatestAuthorizationModel()).thenReturn(failed);

        assertThrows(ExecutionException.class, () -> initializer("classpath:fga/model.json", null)
                .run(new DefaultApplicationArguments()));

        verify(fgaClient, never()).writeAuthorizationModel(any());
    }

    @Test
    void restoresInterruptFlagWhenModelReadIsInterrupted() throws Exception {
        when(fgaClient.readLatestAuthorizationModel()).thenReturn(new CompletableFuture<>());

        assertInterruptRestored(
                () -> initializer("classpath:fga/model.json", null).run(new DefaultApplicationArguments()));
    }

    @Test
    void restoresInterruptFlagWhenModelWriteIsInterrupted() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        when(fgaClient.writeAuthorizationModel(any())).thenReturn(new CompletableFuture<>());

        assertInterruptRestored(
                () -> initializer("classpath:fga/model.json", null).run(new DefaultApplicationArguments()));
    }

    @Test
    void restoresInterruptFlagWhenTupleWriteIsInterrupted() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        var tuples = tupleJson();
        stubResource("classpath:fga/tuples.json", tuples);
        stubTupleRead(List.of());
        stubModelWrite();
        when(fgaClient.write(any(), any(ClientWriteOptions.class))).thenReturn(new CompletableFuture<>());

        assertInterruptRestored(() -> initializer("classpath:fga/model.json", "classpath:fga/tuples.json")
                .run(new DefaultApplicationArguments()));
    }

    @Test
    void failsWhenModelLocationDoesNotExist() throws Exception {
        stubLatestModel(null);
        var resource = mock(Resource.class);
        when(resource.exists()).thenReturn(false);
        when(resourceLoader.getResource("classpath:fga/missing.json")).thenReturn(resource);

        assertThrows(IllegalStateException.class, () -> initializer("classpath:fga/missing.json", null)
                .run(new DefaultApplicationArguments()));

        verify(fgaClient, never()).writeAuthorizationModel(any());
    }
}
