package dev.openfga;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;

import dev.openfga.sdk.errors.FgaApiNotFoundError;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@UnitTest
class OpenFgaExceptionHandlerTest {

    private OpenFgaExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OpenFgaExceptionHandler();
    }

    @Test
    void handle_withRuntimeException() {
        var cause = handler.handle(new RuntimeException("This is the root cause."), "OpenFGA test exception");
        assertThat(cause.getCause().getMessage(), containsString("This is the root cause."));
    }

    @Test
    void handle_withFgaApiNotFoundError() {
        final var responseBody = "{\"apiErrorCode\":\"123\"}";
        var requestId = UUID.randomUUID().toString();

        var cause = new FgaApiNotFoundError("Check endpoint not found.", 404, null, responseBody);
        cause.setRequestId(requestId);
        cause.setApiErrorCode("123");
        cause.setAudience("audience");
        cause.setMethod("POST");
        cause.setRequestUrl("https://openfga.dev/");
        cause.setClientId("integration-test");
        cause.setGrantType("authorization_code");

        var handled = handler.handle(cause, "OpenFGA check exception");

        assertThat(handled.getCause().getMessage(), containsString("Check endpoint not found."));
        assertThat(handled.toString(), not(is(emptyString())));

        var fgaError = handled.getFgaError();
        assertThat(fgaError, notNullValue());
        assertThat(fgaError.getApiErrorCode(), equalTo("123"));
        assertThat(fgaError.getAudience(), equalTo("audience"));
        assertThat(fgaError.getMethod(), equalTo("POST"));
        assertThat(fgaError.getRequestId(), equalTo(requestId));
        assertThat(fgaError.getClientId(), equalTo("integration-test"));
        assertThat(fgaError.getGrantType(), equalTo("authorization_code"));
    }
}
