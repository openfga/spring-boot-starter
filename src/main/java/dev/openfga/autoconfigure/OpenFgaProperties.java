package dev.openfga.autoconfigure;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

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
            String credentialsMethod = getCredentials().getMethod();
            if (credentialsMethod == null) {
                throw new IllegalStateException("credentials method must not be null");
            }
            if (!Set.of("NONE", "API_TOKEN", "CLIENT_CREDENTIALS").contains(credentialsMethod.toUpperCase())) {
                throw new IllegalStateException("credentials method must be either 'NONE', 'API_TOKEN', or 'CLIENT_CREDENTIALS'");
            }

            CredentialsConfiguration credentialsConfig = credentialsProperty.getConfig();
            if ("API_TOKEN".equalsIgnoreCase(credentialsMethod)) {
                if (credentialsConfig == null || credentialsConfig.getApiToken() == null) {
                    throw new IllegalStateException("'API_TOKEN' credentials method specified, but no token specified");
                }
            }
            if ("CLIENT_CREDENTIALS".equalsIgnoreCase(credentialsMethod)) {
                if (credentialsConfig == null || credentialsConfig.getApiTokenIssuer() == null || credentialsConfig.getClientId() == null || credentialsConfig.getClientSecret() == null) {
                    throw new IllegalStateException("'CLIENT_CREDENTIALS' configuration must contain 'client-id', 'client-secret', and 'api-token-issuer'");
                }
            }
        }
    }

    public static class Credentials {

        private String method;
        private CredentialsConfiguration config;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public CredentialsConfiguration getConfig() {
            return config;
        }

        public void setConfig(CredentialsConfiguration config) {
            this.config = config;
        }
    }

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
