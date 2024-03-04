package dev.openfga.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="openfga")
public class OpenFgaProperties {

    private String fgaApiUrl;
    private String fgaStoreId;
    private String fgaApiTokenIssuer;
    private String fgaApiAudience;
    private String fgaClientId;
    private String fgaClientSecret;

    private String fgaAuthorizationModelId;

    public String getFgaApiUrl() {
        return fgaApiUrl;
    }

    public void setFgaApiUrl(String fgaApiUrl) {
        this.fgaApiUrl = fgaApiUrl;
    }

    public String getFgaStoreId() {
        return fgaStoreId;
    }

    public void setFgaStoreId(String fgaStoreId) {
        this.fgaStoreId = fgaStoreId;
    }

    public String getFgaApiTokenIssuer() {
        return fgaApiTokenIssuer;
    }

    public void setFgaApiTokenIssuer(String fgaApiTokenIssuer) {
        this.fgaApiTokenIssuer = fgaApiTokenIssuer;
    }

    public String getFgaApiAudience() {
        return fgaApiAudience;
    }

    public void setFgaApiAudience(String fgaApiAudience) {
        this.fgaApiAudience = fgaApiAudience;
    }

    public String getFgaClientId() {
        return fgaClientId;
    }

    public void setFgaClientId(String fgaClientId) {
        this.fgaClientId = fgaClientId;
    }

    public String getFgaClientSecret() {
        return fgaClientSecret;
    }

    public void setFgaClientSecret(String fgaClientSecret) {
        this.fgaClientSecret = fgaClientSecret;
    }

    public String getFgaAuthorizationModelId() {
        return fgaAuthorizationModelId;
    }

    public void setFgaAuthorizationModelId(String fgaAuthorizationModelId) {
        this.fgaAuthorizationModelId = fgaAuthorizationModelId;
    }
}
