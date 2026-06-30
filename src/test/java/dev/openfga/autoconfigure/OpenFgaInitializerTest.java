package dev.openfga.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.openfga.UnitTest;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadAuthorizationModelResponse;
import dev.openfga.sdk.api.client.model.ClientWriteAuthorizationModelResponse;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.api.client.model.ClientWriteResponse;
import dev.openfga.sdk.api.configuration.ClientWriteOptions;
import dev.openfga.sdk.api.model.AuthorizationModel;
import dev.openfga.sdk.api.model.WriteAuthorizationModelRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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

    private OpenFgaInitializer initializer(String modelLocation, String tuplesLocation) {
        var init = new OpenFgaProperties.Initialization();
        init.setMode(OpenFgaProperties.InitializationMode.EMBEDDED);
        init.setModelLocation(modelLocation);
        init.setTuplesLocation(tuplesLocation);
        return new OpenFgaInitializer(fgaClient, init, resourceLoader, objectMapper);
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

    private void stubModelWrite() throws Exception {
        var response = mock(ClientWriteAuthorizationModelResponse.class);
        when(response.getAuthorizationModelId()).thenReturn("01H...");
        when(fgaClient.writeAuthorizationModel(any())).thenReturn(CompletableFuture.completedFuture(response));
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
    void writesModelAndTuplesWhenTuplesLocationConfigured() throws Exception {
        stubLatestModel(null);
        stubResource("classpath:fga/model.json", "{}".getBytes());
        stubResource("classpath:fga/tuples.json", "[]".getBytes());
        when(objectMapper.readValue("{}".getBytes(), WriteAuthorizationModelRequest.class))
                .thenReturn(new WriteAuthorizationModelRequest());
        when(objectMapper.readValue("[]".getBytes(), ClientWriteRequest.class)).thenReturn(new ClientWriteRequest());
        stubModelWrite();
        when(fgaClient.write(any(), any(ClientWriteOptions.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(ClientWriteResponse.class)));

        initializer("classpath:fga/model.json", "classpath:fga/tuples.json").run(null);

        verify(fgaClient).writeAuthorizationModel(any());
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
