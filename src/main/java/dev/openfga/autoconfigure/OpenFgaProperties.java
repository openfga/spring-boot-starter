package dev.openfga.autoconfigure;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * Properties used to configure an {@link dev.openfga.sdk.api.client.OpenFgaClient}
 */
@ConfigurationProperties(prefix="openfga")
public class OpenFgaProperties implements InitializingBean {

    private String apiUrl;
    private String storeId;
    private String authorizationModelId;

    private Credentials credentials;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getAuthorizationModelId() {
        return authorizationModelId;
    }

    public void setAuthorizationModelId(String authorizationModelId) {
        this.authorizationModelId = authorizationModelId;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validate();
    }

    public void validate() {
        Credentials credentialsProperty = getCredentials();
        if (credentialsProperty != null) {
            CredentialsMethod credentialsMethod = getCredentials().getMethod();
            if (credentialsMethod == null) {
                throw new IllegalStateException("credentials method must not be null");
            }
            CredentialsConfiguration credentialsConfig = credentialsProperty.getConfig();
            switch (credentialsMethod) {
                case NONE  -> {}
                case API_TOKEN -> {
                    if (credentialsConfig == null || credentialsConfig.getApiToken() == null) {
                        throw new IllegalStateException("'API_TOKEN' credentials method specified, but no token specified");
                    }
                }
                case CLIENT_CREDENTIALS -> {
                    if (credentialsConfig == null || credentialsConfig.getApiTokenIssuer() == null || credentialsConfig.getClientId() == null || credentialsConfig.getClientSecret() == null) {
                        throw new IllegalStateException("'CLIENT_CREDENTIALS' configuration must contain 'client-id', 'client-secret', and 'api-token-issuer'");
                    }
                }
                default -> throw new IllegalStateException("credentials method must be either 'NONE', 'API_TOKEN', or 'CLIENT_CREDENTIALS'");
            }
        }
    }

    /**
     * {@link dev.openfga.sdk.api.client.OpenFgaClient} credentials properties
     */
    public static class Credentials {

        private CredentialsMethod method;
        private CredentialsConfiguration config;

        public CredentialsMethod getMethod() {
            return method;
        }

        public void setMethod(CredentialsMethod method) {
            this.method = method;
        }

        public CredentialsConfiguration getConfig() {
            return config;
        }

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
        private String apiToken;
        private String apiTokenIssuer;
        private String apiAudience;
        private String clientId;
        private String clientSecret;

        private String scopes;

        public String getApiTokenIssuer() {
            return apiTokenIssuer;
        }

        public void setApiTokenIssuer(String apiTokenIssuer) {
            this.apiTokenIssuer = apiTokenIssuer;
        }

        public String getApiAudience() {
            return apiAudience;
        }

        public void setApiAudience(String apiAudience) {
            this.apiAudience = apiAudience;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getApiToken() {
            return apiToken;
        }

        public void setApiToken(String apiToken) {
            this.apiToken = apiToken;
        }

        public String getScopes() {
            return scopes;
        }

        public void setScopes(String scopes) {
            this.scopes = scopes;
        }
    }
}
