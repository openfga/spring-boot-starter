package dev.openfga.autoconfigure;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.openfga.sdk.api.configuration.ClientConfiguration;
import dev.openfga.sdk.api.configuration.CredentialsMethod;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OpenFgaAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void noBeanConfiguredIfMissingProperties() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> assertThat(context.containsBean("fgaClient"), is(false)));
    }

    @Test
    void beanConfiguredIfPropertiesPresent() {
        contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://fga-api-url",
                        "openfga.store-id=store ID",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.user-agent=some user agent",
                        "openfga.read-timeout=20",
                        "openfga.connect-timeout=30",
                        "openfga.max-retries=10",
                        "openfga.minimum-retry-delay=60",
                        "openfga.http-version=HTTP_1_1",
                        "openfga.default-headers.some-header=some header value",
                        "openfga.telemetry-configuration.query_duration.fga_client_request_client_id=client request client ID")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> {
                    assertThat(context.containsBean("fgaClient"), is(true));
                    assertThat(context.containsBean("fga"), is(true));
                    assertThat(context.containsBean("defaultHttpClientBuilder"), is(true));
                    assertThat(context.containsBean("defaultHttpClientBuilderCustomizer"), is(true));
                    assertThat(context.containsBean("apiClient"), is(true));
                    ClientConfiguration config = (ClientConfiguration) context.getBean("fgaConfig");
                    assertThat(config.getApiUrl(), is("https://fga-api-url"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getUserAgent(), is("some user agent"));
                    assertThat(config.getReadTimeout(), is(Duration.ofSeconds(20L)));
                    assertThat(config.getConnectTimeout(), is(Duration.ofSeconds(30L)));
                    assertThat(config.getMaxRetries(), is(10));
                    assertThat(config.getMinimumRetryDelay(), is(Duration.ofMinutes(1L)));
                    assertThat(config.getDefaultHeaders(), hasEntry("some-header", "some header value"));
                    assertThat(
                            config.getTelemetryConfiguration().metrics(),
                            hasEntry(
                                    hasProperty("name", is("fga-client.query.duration")),
                                    hasEntry(
                                            hasProperty("name", is("fga-client.request.client_id")),
                                            is(Optional.of("client request client ID")))));
                });
    }

    @Test
    void beanConfiguredForNoAuthorization() {
        contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=NONE",
                        "openfga.credentials.config.api-token=XYZ")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("fgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.NONE));
                });
    }

    @Test
    void beanConfiguredForNoAuthorizationIfCredentialsNotSet() {
        contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("fgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.NONE));
                });
    }

    @Test
    void beanConfiguredForApiToken() {
        contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=API_TOKEN",
                        "openfga.credentials.config.api-token=XYZ")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("fgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.API_TOKEN));
                    assertThat(config.getCredentials().getApiToken().getToken(), is("XYZ"));
                });
    }

    @Test
    void beanConfiguredForOauth2() {
        contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=CLIENT_CREDENTIALS",
                        "openfga.credentials.config.api-token=XYZ", // ignored
                        "openfga.credentials.config.client-id=CLIENT_ID",
                        "openfga.credentials.config.client-secret=CLIENT_SECRET",
                        "openfga.credentials.config.api-token-issuer=API_TOKEN_ISSUER",
                        "openfga.credentials.config.api-audience=API_AUDIENCE",
                        "openfga.credentials.config.scopes=SCOPE1 SCOPE2")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> {
                    ClientConfiguration config = (ClientConfiguration) context.getBean("fgaConfig");
                    assertThat(config.getApiUrl(), is("https://api.fga.example"));
                    assertThat(config.getAuthorizationModelId(), is("authorization model ID"));
                    assertThat(config.getStoreId(), is("store ID"));
                    assertThat(
                            config.getCredentials().getCredentialsMethod(), is(CredentialsMethod.CLIENT_CREDENTIALS));
                    assertThat(config.getCredentials().getApiToken(), is(nullValue()));
                    assertThat(config.getCredentials().getClientCredentials().getClientId(), is("CLIENT_ID"));
                    assertThat(config.getCredentials().getClientCredentials().getClientSecret(), is("CLIENT_SECRET"));
                    assertThat(
                            config.getCredentials().getClientCredentials().getApiTokenIssuer(), is("API_TOKEN_ISSUER"));
                    assertThat(config.getCredentials().getClientCredentials().getApiAudience(), is("API_AUDIENCE"));
                    assertThat(config.getCredentials().getClientCredentials().getScopes(), is("SCOPE1 SCOPE2"));
                });
    }

    @Test
    void failsIfApiTokenMethodSetButNoToken() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=API_TOKEN")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(
                exception.getCause().getMessage(),
                containsString("'API_TOKEN' credentials method specified, but no token specified"));
    }

    @Test
    void failsIfClientCredentialsMethodSetButNotConfigured() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=CLIENT_CREDENTIALS")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(
                exception.getCause().getMessage(),
                containsString(
                        "'CLIENT_CREDENTIALS' configuration must contain 'client-id', 'client-secret', and 'api-token-issuer'"));
    }

    @Test
    void failsIfCredentialsWithNoMethod() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.config.api-token=API_TOKEN")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(exception.getCause().getMessage(), containsString("credentials method must not be null"));
    }

    @Test
    void failsIfCredentialsWithInvalidMethod() {
        assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.credentials.method=INVALID",
                        "openfga.credentials.config.api-token=API_TOKEN")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));
    }

    @Test
    void failsIfReadTimeoutIsNegative() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.read-timeout=-1s")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(exception.getCause().getMessage(), containsString("readTimeout must be positive"));
    }

    @Test
    void failsIfConnectTimeoutIsNegative() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.connect-timeout=-1s")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(exception.getCause().getMessage(), containsString("connectTimeout must be positive"));
    }

    @Test
    void failsIfMinimumRetryDelayIsNegative() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.minimum-retry-delay=-1s")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(exception.getCause().getMessage(), containsString("minimumRetryDelay must be positive"));
    }

    @Test
    void failsIfMaxRetriesIsNegative() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.max-retries=-1")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(exception.getCause().getMessage(), containsString("maxRetries must be positive or zero"));
    }

    @Test
    void failsIfMaxRetriesIsPositiveButMinimumRetryDelayIsNotSet() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> contextRunner
                .withPropertyValues(
                        "openfga.api-url=https://api.fga.example",
                        "openfga.authorization-model-id=authorization model ID",
                        "openfga.store-id=store ID",
                        "openfga.max-retries=1")
                .withConfiguration(AutoConfigurations.of(OpenFgaAutoConfiguration.class))
                .run(context -> context.getBean("fgaConfig")));

        assertThat(
                exception.getCause().getMessage(),
                containsString("minimumRetryDelay must be set if maxRetries is set"));
    }
}
