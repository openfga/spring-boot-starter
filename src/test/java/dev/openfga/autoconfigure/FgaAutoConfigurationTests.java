package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.configuration.ClientConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.is;

public class FgaAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void noBeanConfiguredIfMissingProperties() {
        this.contextRunner
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    assertThat(context.containsBean("openFgaClient"), is(false));
                });
    }

    @Test
    public void beanConfiguredIfPropertiesPresent() {
        this.contextRunner
                .withPropertyValues("openfga.api-url=https://fga-api-url")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    assertThat(context.containsBean("openFgaClient"), is(true));
                });
    }

    @Test
    public void beanConfiguredForApiToken() {
        this.contextRunner
                .withPropertyValues("openfga.api-url=https://fga-api-url",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.api-token=API token"
                )
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("openFgaConfig");
                    assertThat(config.getApiUrl(), is("https://fga-api-url"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                });
    }

    @Test
    public void beanConfiguredForOauth2() {
        this.contextRunner
                .withPropertyValues("openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.client-id=client ID",
                        "openfga.credentials.client-secret=client secret",
                        "openfga.credentials.api-token-issuer=API token issuer",
                        "openfga.credentials.api-audience=API audience",
                        "openfga.credentials.scopes=scope1 scope2"
                )
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("openFgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getClientCredentials().getClientId(), is("client ID"));
                    assertThat(config.getCredentials().getClientCredentials().getClientSecret(), is("client secret"));
                    assertThat(config.getCredentials().getClientCredentials().getApiTokenIssuer(), is("API token issuer"));
                    assertThat(config.getCredentials().getClientCredentials().getApiAudience(), is("API audience"));
                    assertThat(config.getCredentials().getClientCredentials().getScopes(), is("scope1 scope2"));
                    assertThat(config.getCredentials().getClientCredentials().getClientId(), is("client ID"));
                });
    }
}
