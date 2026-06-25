package dev.openfga;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.api.client.model.ClientCheckResponse;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@UnitTest
class OpenFgaTest {
    private final OpenFgaExceptionHandler exceptionHandler = new OpenFgaExceptionHandler();

    @Mock
    private OpenFgaClient mockClient;

    @Mock
    private CompletableFuture<ClientCheckResponse> mockCheckResponseFuture;

    @Mock
    private ClientCheckResponse mockCheckResponse;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void fgaCheckCalled() throws Exception {

        // given
        OpenFga openFga = new OpenFga(mockClient, exceptionHandler);
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
    void usesPrincipalNameAsUserId() throws Exception {
        // given
        Principal principal = () -> "userId";
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        OpenFga openFga = new OpenFga(mockClient, exceptionHandler);
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
    void failsWhenNoUserIdSpecifiedAndNotFoundInContext() {
        // given
        OpenFga openFga = new OpenFga(mockClient, exceptionHandler);

        // when/then
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> openFga.check("document", "docId", "viewer", "user"));
        assertThat(
                exception.getMessage(),
                is("No user provided, and no authentication could be found in the security context"));
    }

    @Test
    void fgaCheckCalledWithContextualTuplesAndContext() throws Exception {

        // given
        OpenFga openFga = new OpenFga(mockClient, exceptionHandler);
        when(mockCheckResponseFuture.get()).thenReturn(mockCheckResponse);
        when(mockClient.check(any(ClientCheckRequest.class))).thenReturn(mockCheckResponseFuture);

        List<ClientTupleKey> contextualTuples = List.of(
                new ClientTupleKey().user("user:userId").relation("member")._object("group:marketing"));
        Map<String, Object> context = Map.of("current_time", "2023-01-01T00:10:01Z");

        // when
        openFga.check("document", "docId", "viewer", "user", "userId", contextualTuples, context);

        // then
        ArgumentCaptor<ClientCheckRequest> argumentCaptor = ArgumentCaptor.forClass(ClientCheckRequest.class);

        verify(mockClient, times(1)).check(any(ClientCheckRequest.class));
        verify(mockClient).check(argumentCaptor.capture());

        ClientCheckRequest request = argumentCaptor.getValue();
        assertThat(request.getObject(), is("document:docId"));
        assertThat(request.getRelation(), is("viewer"));
        assertThat(request.getUser(), is("user:userId"));
        assertThat(request.getContextualTuples(), is(contextualTuples));
        assertThat(request.getContext(), is(context));
    }

    @Test
    void fgaCheckWithContextualTuplesUsesPrincipalNameAsUserId() throws Exception {
        // given
        Principal principal = () -> "userId";
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        OpenFga openFga = new OpenFga(mockClient, exceptionHandler);
        when(mockCheckResponseFuture.get()).thenReturn(mockCheckResponse);
        when(mockClient.check(any(ClientCheckRequest.class))).thenReturn(mockCheckResponseFuture);

        List<ClientTupleKey> contextualTuples = List.of(
                new ClientTupleKey().user("user:userId").relation("member")._object("group:marketing"));
        Map<String, Object> context = Map.of("current_time", "2023-01-01T00:10:01Z");

        // when
        openFga.check("document", "docId", "viewer", "user", contextualTuples, context);

        // then
        ArgumentCaptor<ClientCheckRequest> argumentCaptor = ArgumentCaptor.forClass(ClientCheckRequest.class);

        verify(mockClient, times(1)).check(any(ClientCheckRequest.class));
        verify(mockClient).check(argumentCaptor.capture());

        ClientCheckRequest request = argumentCaptor.getValue();
        assertThat(request.getObject(), is("document:docId"));
        assertThat(request.getRelation(), is("viewer"));
        assertThat(request.getUser(), is("user:userId"));
        assertThat(request.getContextualTuples(), is(contextualTuples));
        assertThat(request.getContext(), is(context));
    }

    @Test
    void failsWhenNoUserIdSpecifiedForContextualCheckAndNotFoundInContext() {
        // given
        OpenFga openFga = new OpenFga(mockClient, exceptionHandler);

        // when/then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> openFga.check("document", "docId", "viewer", "user", null, null));
        assertThat(
                exception.getMessage(),
                is("No user provided, and no authentication could be found in the security context"));
    }

    @Test
    void failsWhenCheckHasException() throws Exception {
        // given
        OpenFga openFga = new OpenFga(mockClient, exceptionHandler);

        // when
        when(mockClient.check(any(ClientCheckRequest.class))).thenThrow(FgaInvalidParameterException.class);

        // then
        OpenFgaException exception = assertThrows(
                OpenFgaException.class, () -> openFga.check("document", "docId", "viewer", "user", "userId"));
        assertThat(exception.getMessage(), is("Error performing FGA check"));
        assertThat(exception.getCause(), is(instanceOf(FgaInvalidParameterException.class)));
    }
}
