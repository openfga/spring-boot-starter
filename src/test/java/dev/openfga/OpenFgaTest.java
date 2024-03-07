package dev.openfga;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientCheckResponse;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OpenFgaTest {

    @Mock
    private OpenFgaClient mockClient;

    @Mock
    private CompletableFuture<ClientCheckResponse> mockCheckResponseFuture;

    @Mock
    private ClientCheckResponse mockCheckResponse;

    @Test
    public void fgaCheckCalled() throws Exception {
        // given
        when(mockCheckResponseFuture.get()).thenReturn(mockCheckResponse);
        when(mockClient.check(any(ClientCheckRequest.class))).thenReturn(mockCheckResponseFuture);

        // when
        OpenFga openFga = new OpenFga(mockClient);
        openFga.check("document", "docId", "viewer", "user", "userId");

        // then
        ArgumentCaptor<ClientCheckRequest> argumentCaptor = ArgumentCaptor.forClass(ClientCheckRequest.class);

        verify(mockClient, times(1)).check(any(ClientCheckRequest.class));
        verify(mockClient).check(argumentCaptor.capture());

        ClientCheckRequest request = argumentCaptor.getValue();
        assertThat(request.getObject(), is("document:docId"));
        assertThat(request.getRelation(), is("viewer"));
        assertThat(request.getUser(), is("user:userId"));
    }
}
