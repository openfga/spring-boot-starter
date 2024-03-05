package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ApiToken;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.ClientCredentials;
import dev.openfga.sdk.api.configuration.Credentials;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnFgaProperties
@EnableConfigurationProperties(OpenFgaProperties.class)
public class OpenFgaAutoConfiguration {

    private final OpenFgaProperties openFgaProperties;

    public OpenFgaAutoConfiguration(OpenFgaProperties openFgaProperties) {
        this.openFgaProperties = openFgaProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientConfiguration openFgaConfig() {
        var credentials = new Credentials();

        var credentialsProperties = openFgaProperties.getCredentials();

        if (credentialsProperties != null) {
            if (credentialsProperties.getApiToken() != null) {
                credentials.setApiToken(new ApiToken(credentialsProperties.getApiToken()));
            } else {
                ClientCredentials clientCredentials = new ClientCredentials()
                        .clientId(credentialsProperties.getClientId())
                        .clientSecret(credentialsProperties.getClientSecret())
                        .apiTokenIssuer(credentialsProperties.getApiTokenIssuer())
                        .apiAudience(credentialsProperties.getApiAudience())
                        .scopes(credentialsProperties.getScopes());

                credentials.setClientCredentials(clientCredentials);
            }
        }

        return new ClientConfiguration()
                .apiUrl(openFgaProperties.getApiUrl())
                .storeId(openFgaProperties.getStoreId())
                .authorizationModelId(openFgaProperties.getAuthorizationModelId())
                .credentials(credentials);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenFgaClient openFgaClient(ClientConfiguration configuration) {
        try {
            return new OpenFgaClient(configuration);
        } catch (FgaInvalidParameterException e) {
            throw new BeanCreationException("Failed to create OpenFgaClient", e);
        }
    }
}
