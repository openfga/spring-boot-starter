package dev.openfga.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="openfga")
public class OpenFgaProperties {

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

    public static class Credentials {

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
