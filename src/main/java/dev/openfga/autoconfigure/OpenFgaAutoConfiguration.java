package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.client.OpenFgaClient;
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

        if (openFgaProperties.getFgaClientId() != null) {
            credentials = new Credentials(new ClientCredentials()
                    .apiAudience(openFgaProperties.getFgaApiAudience())
                    .apiTokenIssuer(openFgaProperties.getFgaApiTokenIssuer())
                    .clientId(openFgaProperties.getFgaClientId())
                    .clientSecret(openFgaProperties.getFgaClientSecret()));
        }

        return new ClientConfiguration()
                .apiUrl(openFgaProperties.getFgaApiUrl())
                .storeId(openFgaProperties.getFgaStoreId())
                .authorizationModelId(openFgaProperties.getFgaAuthorizationModelId())
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
