package dev.openfga.autoconfigure;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.util.StringUtils;

/**
 * Properties used to configure an {@link dev.openfga.sdk.api.client.OpenFgaClient}
 */
@ConfigurationProperties(prefix = "openfga")
public class OpenFgaProperties implements InitializingBean {

    /**
     * URL to OpenFGA instance.
     * If configured, beans will be initialized.
     */
    private String apiUrl;

    /**
     * The ID of the store to use.
     */
    private String storeId;

    /**
     * The ID of the authorization model to use.
     */
    private String authorizationModelId;

    /**
     * The credentials for the OpenFGA client.
     */
    private Credentials credentials;

    /**
     * The HTTP user agent header to use for requests, default openfga-sdk java/version
     */
    private String userAgent;

    /**
     * The read timeout for request. Default unit is seconds. Defaults to 10 seconds.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration readTimeout;

    /**
     * The connect timeout for requests. Default unit is seconds. Defaults to 10 seconds.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration connectTimeout;

    /**
     * The maximum number of retries to attempt. Defaults to no retries. If you set this to a positive value, ensure that you also set the {@link OpenFgaProperties#minimumRetryDelay} property.
     */
    private Integer maxRetries;

    /**
     * The maximum delay between retries. Default unit is seconds. Must be set if {@link OpenFgaProperties#maxRetries} is set.
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration minimumRetryDelay;

    /**
     * The HTTP version to use for requests. Default is HTTP version 2
     */
    private HttpVersion httpVersion;

    /**
     * The default headers to use for requests.
     */
    private Map<String, String> defaultHeaders;

    /**
     * The telemetry configuration for the client. See {@link TelemetryMetric} and {@link TelemetryAttribute} as well as <a href="https://openfga.dev/docs/getting-started/configure-telemetry">Configure SDK Client Telemetry</a>
     */
    private Map<TelemetryMetric, Map<TelemetryAttribute, Object>> telemetryConfiguration;

    /**
     * Gets the URL to the OpenFGA instance.
     *
     * @return the API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Sets the URL to the OpenFGA instance.
     *
     * @param apiUrl the API URL to set
     */
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    /**
     * Gets the ID of the store to use.
     *
     * @return the store ID
     */
    public String getStoreId() {
        return storeId;
    }

    /**
     * Sets the ID of the store to use.
     *
     * @param storeId the store ID to set
     */
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    /**
     * Gets the ID of the authorization model to use.
     *
     * @return the authorization model ID
     */
    public String getAuthorizationModelId() {
        return authorizationModelId;
    }

    /**
     * Sets the ID of the authorization model to use.
     *
     * @param authorizationModelId the authorization model ID to set
     */
    public void setAuthorizationModelId(String authorizationModelId) {
        this.authorizationModelId = authorizationModelId;
    }

    /**
     * Gets the credentials for the OpenFGA client.
     *
     * @return the credentials
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the credentials for the OpenFGA client.
     *
     * @param credentials the credentials to set
     */
    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Gets the HTTP user agent header to use for requests.
     *
     * @return the user agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the HTTP user agent header to use for requests.
     *
     * @param userAgent the user agent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Gets the read timeout for requests.
     *
     * @return the read timeout
     */
    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * Sets the read timeout for requests.
     *
     * @param readTimeout the read timeout to set
     */
    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Gets the connect timeout for requests.
     *
     * @return the connect timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Sets the connect timeout for requests.
     *
     * @param connectTimeout the connect timeout to set
     */
    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Gets the maximum number of retries to attempt.
     *
     * @return the maximum number of retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Sets the maximum number of retries to attempt.
     *
     * @param maxRetries the maximum number of retries to set
     */
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    /**
     * Gets the maximum delay between retries.
     *
     * @return the minimum retry delay
     */
    public Duration getMinimumRetryDelay() {
        return minimumRetryDelay;
    }

    /**
     * Sets the maximum delay between retries.
     *
     * @param minimumRetryDelay the minimum retry delay to set
     */
    public void setMinimumRetryDelay(Duration minimumRetryDelay) {
        this.minimumRetryDelay = minimumRetryDelay;
    }

    /**
     * Gets the HTTP version to use for requests.
     *
     * @return the HTTP version
     */
    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    /**
     * Sets the HTTP version to use for requests.
     *
     * @param httpVersion the HTTP version to set
     */
    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    /**
     * Gets the default headers to use for requests.
     *
     * @return the default headers
     */
    public Map<String, String> getDefaultHeaders() {
        return defaultHeaders;
    }

    /**
     * Sets the default headers to use for requests.
     *
     * @param defaultHeaders the default headers to set
     */
    public void setDefaultHeaders(Map<String, String> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    /**
     * Gets the telemetry configuration for the client.
     *
     * @return the telemetry configuration
     */
    public Map<TelemetryMetric, Map<TelemetryAttribute, Object>> getTelemetryConfiguration() {
        return telemetryConfiguration;
    }

    /**
     * Sets the telemetry configuration for the client.
     *
     * @param telemetryConfiguration the telemetry configuration to set
     */
    public void setTelemetryConfiguration(
            Map<TelemetryMetric, Map<TelemetryAttribute, Object>> telemetryConfiguration) {
        this.telemetryConfiguration = telemetryConfiguration;
    }

    @Override
    public void afterPropertiesSet() {
        validate();
    }

    /**
     * Validates the properties. Throws an {@link IllegalStateException} if any property is invalid. This method is called
     * after the properties are set. If you are using this class outside a Spring context, you should call this method
     * after setting the properties.
     */
    public void validate() {
        Credentials credentialsProperty = credentials;
        if (credentialsProperty != null) {
            CredentialsMethod credentialsMethod = credentials.getMethod();
            if (credentialsMethod == null) {
                throw new IllegalStateException("credentials method must not be null");
            }
            CredentialsConfiguration credentialsConfig = credentialsProperty.getConfig();
            if (credentialsMethod == CredentialsMethod.API_TOKEN) {
                if (credentialsConfig == null || !StringUtils.hasText(credentialsConfig.getApiToken())) {
                    throw new IllegalStateException("'API_TOKEN' credentials method specified, but no token specified");
                }
            } else if (credentialsMethod == CredentialsMethod.CLIENT_CREDENTIALS) {
                if (credentialsConfig == null
                        || !StringUtils.hasText(credentialsConfig.getApiTokenIssuer())
                        || !StringUtils.hasText(credentialsConfig.getClientId())
                        || !StringUtils.hasText(credentialsConfig.getClientSecret())) {
                    throw new IllegalStateException(
                            "'CLIENT_CREDENTIALS' configuration must contain 'client-id', 'client-secret', and 'api-token-issuer'");
                }
            } else if (credentialsMethod != CredentialsMethod.NONE) {
                throw new IllegalStateException(
                        "credentials method must be either 'NONE', 'API_TOKEN', or 'CLIENT_CREDENTIALS'");
            }
        }
        assertPositivity(readTimeout, "readTimeout");
        assertPositivity(connectTimeout, "connectTimeout");
        assertPositivity(minimumRetryDelay, "minimumRetryDelay");
        if (maxRetries != null) {
            if (maxRetries < 0) {
                throw new IllegalStateException("maxRetries must be positive or zero");
            }
            if (minimumRetryDelay == null) {
                throw new IllegalStateException("minimumRetryDelay must be set if maxRetries is set");
            }
        }
    }

    private static void assertPositivity(Duration duration, String fieldName) {
        if (duration != null && duration.isNegative()) {
            throw new IllegalStateException("%s must be positive".formatted(fieldName));
        }
    }

    /**
     * {@link dev.openfga.sdk.api.client.OpenFgaClient} credentials properties
     */
    public static class Credentials {

        /**
         * The authentication method to use for the OpenFGA client.
         */
        private CredentialsMethod method;

        /**
         * Authentication config for the OpenFGA client.
         */
        private CredentialsConfiguration config;

        /**
         * Gets the authentication method to use for the OpenFGA client.
         *
         * @return the credentials method
         */
        public CredentialsMethod getMethod() {
            return method;
        }

        /**
         * Sets the authentication method to use for the OpenFGA client.
         *
         * @param method the credentials method to set
         */
        public void setMethod(CredentialsMethod method) {
            this.method = method;
        }

        /**
         * Gets the authentication config for the OpenFGA client.
         *
         * @return the credential configuration
         */
        public CredentialsConfiguration getConfig() {
            return config;
        }

        /**
         * Sets the authentication config for the OpenFGA client.
         *
         * @param config the credential configuration to set
         */
        public void setConfig(CredentialsConfiguration config) {
            this.config = config;
        }
    }

    /**
     * OpenFgaClient credentials methods
     */
    public enum CredentialsMethod {

        /**
         * No authentication
         */
        NONE,

        /**
         * A static API token. In OAuth2 terms, this indicates an "access token"
         * that will be used to make a request. When used, an {@code api-token} must
         * also be configured.
         */
        API_TOKEN,

        /**
         * OAuth2 client credentials that can be used to acquire an OAuth2 access
         * token. When used, you must also configure {@link CredentialsConfiguration}.
         */
        CLIENT_CREDENTIALS
    }

    /**
     * {@link dev.openfga.sdk.api.client.OpenFgaClient} credentials configuration properties
     */
    public static class CredentialsConfiguration {
        /**
         * The API token used to authenticate the client at OpenFGA.
         * Required, if {@code API_TOKEN} method should be used.
         */
        private String apiToken;

        /**
         * The API token issuer, used as an additional check.
         * Required, if {@code CLIENT_CREDENTIALS} method should be used.
         */
        private String apiTokenIssuer;

        /**
         * The API audience.
         * If configured and if {@code CLIENT_CREDENTIALS} is used,
         * the client will use this audience to obtain an access token.
         */
        private String apiAudience;

        /**
         * The OAuth2 client ID.
         * Required, if {@code CLIENT_CREDENTIALS} method should be used.
         */
        private String clientId;

        /**
         * The OAuth2 client secret.
         * Required, if {@code CLIENT_CREDENTIALS} method should be used.
         */
        private String clientSecret;

        /**
         * Space seperated list of OAuth2 scopes used to
         * obtain an access token.
         */
        private String scopes;

        /**
         * Gets the API token used to authenticate the client at OpenFGA.
         *
         * @return the API token
         */
        public String getApiTokenIssuer() {
            return apiTokenIssuer;
        }

        /**
         * Sets the API token used to authenticate the client at OpenFGA.
         *
         * @param apiTokenIssuer the API token to set
         */
        public void setApiTokenIssuer(String apiTokenIssuer) {
            this.apiTokenIssuer = apiTokenIssuer;
        }

        /**
         * Gets the API audience.
         *
         * @return the API audience
         */
        public String getApiAudience() {
            return apiAudience;
        }

        /**
         * Sets the API audience.
         *
         * @param apiAudience the API audience to set
         */
        public void setApiAudience(String apiAudience) {
            this.apiAudience = apiAudience;
        }

        /**
         * Gets the OAuth2 client ID.
         *
         * @return the client ID
         */
        public String getClientId() {
            return clientId;
        }

        /**
         * Sets the OAuth2 client ID.
         *
         * @param clientId the client ID to set
         */
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        /**
         * Gets the OAuth2 client secret.
         *
         * @return the client secret
         */
        public String getClientSecret() {
            return clientSecret;
        }

        /**
         * Sets the OAuth2 client secret.
         *
         * @param clientSecret the client secret to set
         */
        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        /**
         * Gets the OAuth2 scopes used to get an access token.
         *
         * @return the scopes
         */
        public String getApiToken() {
            return apiToken;
        }

        /**
         * Sets the OAuth2 scopes used to get an access token.
         *
         * @param apiToken the scopes to set
         */
        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

        /**
         * Gets the OAuth2 scopes used to get an access token.
         *
         * @return the scopes
         */
        public String getScopes() {
            return scopes;
        }

        /**
         * Sets the OAuth2 scopes used to get an access token.
         *
         * @param scopes the scopes to set
         */
        public void setScopes(String scopes) {
            this.scopes = scopes;
        }
    }
}
