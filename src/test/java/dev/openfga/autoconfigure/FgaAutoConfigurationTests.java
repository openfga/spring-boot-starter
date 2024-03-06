package dev.openfga.autoconfigure;

import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.CredentialsMethod;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    public void beanConfiguredForNoAuthorization() {
        this.contextRunner
                .withPropertyValues("openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=NONE",
                        "openfga.credentials.config.api-token=XYZ"
                )
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("openFgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.NONE));
                });
    }

    @Test
    public void beanConfiguredForNoAuthorizationIfCredentialsNotSet() {
        this.contextRunner
                .withPropertyValues("openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID"
                )
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("openFgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.NONE));
                });
    }

    @Test
    public void beanConfiguredForApiToken() {
        this.contextRunner
                .withPropertyValues("openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=API_TOKEN",
                        "openfga.credentials.config.api-token=XYZ"
                )
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("openFgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.API_TOKEN));
                    assertThat(config.getCredentials().getApiToken().getToken(), is("XYZ"));
                });
    }

    @Test
    public void beanConfiguredForOauth2() {
        this.contextRunner
                .withPropertyValues("openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=CLIENT_CREDENTIALS",
                        "openfga.credentials.config.api-token=XYZ", // ignored
                        "openfga.credentials.config.client-id=CLIENT_ID",
                        "openfga.credentials.config.client-secret=CLIENT_SECRET",
                        "openfga.credentials.config.api-token-issuer=API_TOKEN_ISSUER",
                        "openfga.credentials.config.api-audience=API_AUDIENCE",
                        "openfga.credentials.config.scopes=SCOPE1 SCOPE2"
                )
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run((context) -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("openFgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.CLIENT_CREDENTIALS));
                    assertThat(config.getCredentials().getApiToken(), is(nullValue()));
                    assertThat(config.getCredentials().getClientCredentials().getClientId(), is("CLIENT_ID"));
                    assertThat(config.getCredentials().getClientCredentials().getClientSecret(), is("CLIENT_SECRET"));
                    assertThat(config.getCredentials().getClientCredentials().getApiTokenIssuer(), is("API_TOKEN_ISSUER"));
                    assertThat(config.getCredentials().getClientCredentials().getApiAudience(), is("API_AUDIENCE"));
                    assertThat(config.getCredentials().getClientCredentials().getScopes(), is("SCOPE1 SCOPE2"));
                });
    }

    @Test
    public void failsIfApiTokenMethodSetButNoToken() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            this.contextRunner
                    .withPropertyValues("openfga.api-url=https://api.fga.example",
                            "openfga.authorization-model-id=authorization model ID",
                            "openfga.store-id=store ID",
                            "openfga.credentials.method=API_TOKEN"
                    )
                    .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                    .run((context) -> context.getBean("openFgaConfig"));
        });

        assertThat(exception.getCause().getMessage(), containsString("'API_TOKEN' credentials method specified, but no token specified"));
    }

    @Test
    public void failsIfClientCredentialsMethodSetButNotConfigured() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            this.contextRunner
                    .withPropertyValues("openfga.api-url=https://api.fga.example",
                            "openfga.authorization-model-id=authorization model ID",
                            "openfga.store-id=store ID",
                            "openfga.credentials.method=CLIENT_CREDENTIALS"
                    )
                    .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                    .run((context) -> context.getBean("openFgaConfig"));
        });

        assertThat(exception.getCause().getMessage(), containsString("'CLIENT_CREDENTIALS' configuration must contain 'client-id', 'client-secret', and 'api-token-issuer'"));
    }

    @Test
    public void failsIfCredentialsWithNoMethod() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            this.contextRunner
                    .withPropertyValues("openfga.api-url=https://api.fga.example",
                            "openfga.authorization-model-id=authorization model ID",
                            "openfga.store-id=store ID",
                            "openfga.credentials.config.api-token=API_TOKEN"
                    )
                    .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                    .run((context) -> context.getBean("openFgaConfig"));
        });

        assertThat(exception.getCause().getMessage(), containsString("credentials method must not be null"));
    }

    @Test
    public void failsIfCredentialsWithInvalidMethod() {
        assertThrows(IllegalStateException.class, () -> {
            this.contextRunner
                    .withPropertyValues("openfga.api-url=https://api.fga.example",
                            "openfga.authorization-model-id=authorization model ID",
                            "openfga.store-id=store ID",
                            "openfga.credentials.method=INVALID",
                            "openfga.credentials.config.api-token=API_TOKEN"
                    )
                    .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                    .run((context) -> context.getBean("openFgaConfig"));
        });
    }
}
