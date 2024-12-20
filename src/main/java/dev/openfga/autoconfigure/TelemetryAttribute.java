package dev.openfga.autoconfigure;

import dev.openfga.sdk.telemetry.Attribute;
import dev.openfga.sdk.telemetry.Attributes;
import java.util.Objects;

/**
 * The telemetry attributes that can be used to log telemetry data.
 *
 * See <a href="https://openfga.dev/docs/getting-started/configure-telemetry">OpenFGA Telemetry</a> for more information.
 */
public enum TelemetryAttribute {
    /**
     * The client ID used in the request, if applicable.
     */
    FGA_CLIENT_REQUEST_CLIENT_ID(Attributes.FGA_CLIENT_REQUEST_CLIENT_ID),
    /**
     * The FGA method / action of the request.
     */
    FGA_CLIENT_REQUEST_METHOD(Attributes.FGA_CLIENT_REQUEST_METHOD),
    /**
     * The authorization model ID used in the request, if applicable.
     */
    FGA_CLIENT_REQUEST_MODEL_ID(Attributes.FGA_CLIENT_REQUEST_MODEL_ID),
    /**
     * The store ID used in the request, if applicable.
     */
    FGA_CLIENT_REQUEST_STORE_ID(Attributes.FGA_CLIENT_REQUEST_STORE_ID),
    /**
     * The authorization model ID used by the server when evaluating the request, if applicable.
     */
    FGA_CLIENT_RESPONSE_MODEL_ID(Attributes.FGA_CLIENT_RESPONSE_MODEL_ID),
    /**
     * The HTTP host used in the request, e.g. "example.com".
     */
    HTTP_HOST(Attributes.HTTP_HOST),
    /**
     * The HTTP method used in the request, e.g. "GET".
     */
    HTTP_REQUEST_METHOD(Attributes.HTTP_REQUEST_METHOD),
    /**
     * The number of times the request was retried
     */
    HTTP_REQUEST_RESEND_COUNT(Attributes.HTTP_REQUEST_RESEND_COUNT),
    /**
     * The HTTP status code returned by the server for the request, e.g. 200.
     */
    HTTP_RESPONSE_STATUS_CODE(Attributes.HTTP_RESPONSE_STATUS_CODE),
    /**
     * The complete URL used in the request, e.g. "https://example.com/path".
     */
    URL_FULL(Attributes.URL_FULL),
    /**
     * The scheme used in the request, e.g. "https".
     */
    URL_SCHEME(Attributes.URL_SCHEME),
    /**
     * The user agent used in the request, e.g. "Mozilla/5.0".
     */
    USER_AGENT(Attributes.USER_AGENT);

    private final Attribute attribute;

    TelemetryAttribute(Attribute attribute) {
        this.attribute = Objects.requireNonNull(attribute);
    }

    Attribute getAttribute() {
        return attribute;
    }
}
