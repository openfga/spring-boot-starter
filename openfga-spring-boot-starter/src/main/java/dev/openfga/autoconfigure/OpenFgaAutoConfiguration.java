package dev.openfga.autoconfigure;

import dev.openfga.OpenFga;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Configures an {@code openFgaClient} and {@code openFga} beans based
 * on configuration values. The beans will only be created if the
 * {@link OpenFgaClient} is present on the classpath, and the
 * {@code openfga.api-url} is specified.
 */
@AutoConfiguration
@ConditionalOnFgaProperties
@EnableConfigurationProperties(OpenFgaProperties.class)
public class OpenFgaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(OpenFgaConnectionDetails.class)
    PropertiesOpenFgaConnectionDetails openFgaConnectionDetails(OpenFgaProperties openFgaProperties) {
        return new PropertiesOpenFgaConnectionDetails(openFgaProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientConfiguration fgaConfig(OpenFgaProperties properties, OpenFgaConnectionDetails connectionDetails) {
        var credentials = new Credentials();

        var credentialsProperties = properties.getCredentials();

        if (credentialsProperties != null) {
            if (OpenFgaProperties.CredentialsMethod.API_TOKEN.equals(credentialsProperties.getMethod())) {
                credentials.setCredentialsMethod(CredentialsMethod.API_TOKEN);
                credentials.setApiToken(
                        new ApiToken(credentialsProperties.getConfig().getApiToken()));
            } else if (OpenFgaProperties.CredentialsMethod.CLIENT_CREDENTIALS.equals(
                    credentialsProperties.getMethod())) {
                ClientCredentials clientCredentials = new ClientCredentials()
                        .clientId(credentialsProperties.getConfig().getClientId())
                        .clientSecret(credentialsProperties.getConfig().getClientSecret())
                        .apiTokenIssuer(credentialsProperties.getConfig().getApiTokenIssuer())
                        .apiAudience(credentialsProperties.getConfig().getApiAudience())
                        .scopes(credentialsProperties.getConfig().getScopes());

                credentials.setCredentialsMethod(CredentialsMethod.CLIENT_CREDENTIALS);
                credentials.setClientCredentials(clientCredentials);
            }
        }

        return new ClientConfiguration()
                .apiUrl(connectionDetails.getApiUrl())
                .storeId(properties.getStoreId())
                .authorizationModelId(properties.getAuthorizationModelId())
                .credentials(credentials);
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenFgaClient fgaClient(ClientConfiguration configuration) {
        try {
            return new OpenFgaClient(configuration);
        } catch (FgaInvalidParameterException e) {
            throw new BeanCreationException("Failed to create OpenFgaClient", e);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public OpenFga fga(OpenFgaClient openFgaClient) {
        return new OpenFga(openFgaClient);
    }

    static class PropertiesOpenFgaConnectionDetails implements OpenFgaConnectionDetails {

        private final OpenFgaProperties openFgaProperties;

        public PropertiesOpenFgaConnectionDetails(OpenFgaProperties openFgaProperties) {
            this.openFgaProperties = openFgaProperties;
        }

        @Override
        public String getApiUrl() {
            return this.openFgaProperties.getApiUrl();
        }
    }
}
