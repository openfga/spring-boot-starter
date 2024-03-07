package dev.openfga;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientCheckResponse;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.security.Principal;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        OpenFga openFga = new OpenFga(mockClient);
        when(mockCheckResponseFuture.get()).thenReturn(mockCheckResponse);
        when(mockClient.check(any(ClientCheckRequest.class))).thenReturn(mockCheckResponseFuture);

        // when
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

    @Test
    public void usesPrincipalNameAsUserId() throws Exception {
        // given
        Principal principal = () -> "userId";
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        OpenFga openFga = new OpenFga(mockClient);
        when(mockCheckResponseFuture.get()).thenReturn(mockCheckResponse);
        when(mockClient.check(any(ClientCheckRequest.class))).thenReturn(mockCheckResponseFuture);

        // when
        openFga.check("document", "docId", "viewer", "user");

        // then
        ArgumentCaptor<ClientCheckRequest> argumentCaptor = ArgumentCaptor.forClass(ClientCheckRequest.class);

        verify(mockClient, times(1)).check(any(ClientCheckRequest.class));
        verify(mockClient).check(argumentCaptor.capture());

        ClientCheckRequest request = argumentCaptor.getValue();
        assertThat(request.getObject(), is("document:docId"));
        assertThat(request.getRelation(), is("viewer"));
        assertThat(request.getUser(), is("user:userId"));
    }

    @Test
    public void failsWhenNoUserIdSpecifiedAndNotFoundInContext() throws Exception {
        // given
        OpenFga openFga = new OpenFga(mockClient);

        // when/then
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> openFga.check("document", "docId", "viewer", "user"));
        assertThat(
                exception.getMessage(),
                is("No user provided, and no authentication could be found in the security context"));
    }

    @Test
    public void failsWhenCheckHasException() throws Exception {
        // given
        OpenFga openFga = new OpenFga(mockClient);

        // when
        when(mockClient.check(any(ClientCheckRequest.class))).thenThrow(FgaInvalidParameterException.class);

        // then
        assertThrows(OpenFgaCheckException.class, () -> openFga.check("document", "docId", "viewer", "user", "userId"));
    }
}
