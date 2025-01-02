package dev.openfga.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.openfga.OpenFga;
import dev.openfga.sdk.api.client.ApiClient;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.*;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.net.http.HttpClient;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
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

    /**
     * Configures the OpenFGA client with the provided properties.
     *
     * @param openFgaProperties the configuration properties for OpenFGA
     * @return the configured {@link ClientConfiguration} bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientConfiguration fgaConfig(OpenFgaProperties openFgaProperties) {
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        PropertyMapper map = PropertyMapper.get();
        map.from(openFgaProperties::getCredentials)
                .whenNonNull()
                .as(OpenFgaAutoConfiguration::toCredentials)
                .to(clientConfiguration::credentials);
        map.from(openFgaProperties::getApiUrl).whenHasText().to(clientConfiguration::apiUrl);
        map.from(openFgaProperties::getStoreId).whenHasText().to(clientConfiguration::storeId);
        map.from(openFgaProperties::getAuthorizationModelId)
                .whenHasText()
                .to(clientConfiguration::authorizationModelId);
        map.from(openFgaProperties::getUserAgent).whenHasText().to(clientConfiguration::userAgent);
        map.from(openFgaProperties::getReadTimeout).whenNonNull().to(clientConfiguration::readTimeout);
        map.from(openFgaProperties::getConnectTimeout).whenNonNull().to(clientConfiguration::connectTimeout);
        map.from(openFgaProperties::getMaxRetries).whenNonNull().to(clientConfiguration::maxRetries);
        map.from(openFgaProperties::getMinimumRetryDelay).whenNonNull().to(clientConfiguration::minimumRetryDelay);
        map.from(openFgaProperties::getDefaultHeaders).whenNonNull().to(clientConfiguration::defaultHeaders);
        map.from(openFgaProperties::getTelemetryConfiguration)
                .whenNonNull()
                .as(OpenFgaAutoConfiguration::toTelemetryConfiguration)
                .to(clientConfiguration::telemetryConfiguration);
        return clientConfiguration;
    }

    private static Credentials toCredentials(OpenFgaProperties.Credentials credentialsProperties) {
        Credentials credentials = new Credentials();
        if (OpenFgaProperties.CredentialsMethod.API_TOKEN == credentialsProperties.getMethod()) {
            credentials.setCredentialsMethod(CredentialsMethod.API_TOKEN);
            credentials.setApiToken(
                    new ApiToken(credentialsProperties.getConfig().getApiToken()));
        } else if (OpenFgaProperties.CredentialsMethod.CLIENT_CREDENTIALS == credentialsProperties.getMethod()) {
            ClientCredentials clientCredentials = new ClientCredentials()
                    .clientId(credentialsProperties.getConfig().getClientId())
                    .clientSecret(credentialsProperties.getConfig().getClientSecret())
                    .apiTokenIssuer(credentialsProperties.getConfig().getApiTokenIssuer())
                    .apiAudience(credentialsProperties.getConfig().getApiAudience())
                    .scopes(credentialsProperties.getConfig().getScopes());
            credentials.setCredentialsMethod(CredentialsMethod.CLIENT_CREDENTIALS);
            credentials.setClientCredentials(clientCredentials);
        }
        return credentials;
    }

    private static TelemetryConfiguration toTelemetryConfiguration(
            Map<TelemetryMetric, Map<TelemetryAttribute, Object>> telemetryConfiguration) {
        return new TelemetryConfiguration()
                .metrics(telemetryConfiguration.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().getMetric(), metric -> metric.getValue().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                metricConfig ->
                                                        metricConfig.getKey().getAttribute(),
                                                metricConfig -> Optional.ofNullable(metricConfig.getValue()))))));
    }

    /**
     * Provides a default {@link HttpClient.Builder} bean if none is already defined and the
     * {@code openfga.http-version} property is set.
     *
     * @return a default {@link HttpClient.Builder} bean
     */
    @Bean
    @ConditionalOnProperty(name = "openfga.http-version")
    @ConditionalOnMissingBean
    HttpClient.Builder defaultHttpClientBuilder() {
        return HttpClient.newBuilder();
    }

    /**
     * Provides a default {@link HttpClientBuilderCustomizer} bean if none is already defined.
     *
     * @param openFgaProperties the configuration properties for OpenFGA
     * @return a customizer for the {@link HttpClient.Builder}
     */
    @Bean
    @ConditionalOnMissingBean
    HttpClientBuilderCustomizer defaultHttpClientBuilderCustomizer(OpenFgaProperties openFgaProperties) {
        return builder -> {
            if (openFgaProperties.getHttpVersion() != null) {
                builder.version(openFgaProperties.getHttpVersion().getVersion());
            }
        };
    }

    /**
     * Creates an {@link ApiClient} bean if none exists.
     *
     * @param httpClientBuilderProvider provides the {@link HttpClient.Builder} bean
     * @param objectMapperProvider provides the {@link ObjectMapper} bean
     * @param httpClientBuilderCustomizer customizes the {@link HttpClient.Builder}
     * @return the configured {@link ApiClient} bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiClient apiClient(
            ObjectProvider<HttpClient.Builder> httpClientBuilderProvider,
            ObjectProvider<ObjectMapper> objectMapperProvider,
            HttpClientBuilderCustomizer httpClientBuilderCustomizer) {

        if (httpClientBuilderProvider.getIfAvailable() == null && objectMapperProvider.getIfAvailable() == null) {
            return new ApiClient();
        }
        HttpClient.Builder httpClientBuilder = httpClientBuilderProvider.getIfAvailable(HttpClient::newBuilder);
        httpClientBuilderCustomizer.customize(httpClientBuilder);
        return new ApiClient(
                httpClientBuilder,
                objectMapperProvider.getIfAvailable(OpenFgaAutoConfiguration::createDefaultObjectMapper));
    }

    private static ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new JsonNullableModule());
        return mapper;
    }

    /**
     * Defines an {@link OpenFgaClient} bean if not already present in the context.
     *
     * @param configuration the {@link ClientConfiguration} bean containing OpenFGA settings
     * @param apiClient the {@link ApiClient} bean for making API requests
     * @return a configured {@link OpenFgaClient} bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenFgaClient fgaClient(ClientConfiguration configuration, ApiClient apiClient) {
        try {
            return new OpenFgaClient(configuration, apiClient);
        } catch (FgaInvalidParameterException e) {
            throw new BeanCreationException("Failed to create OpenFgaClient", e);
        }
    }

    /**
     * Creates an {@link OpenFga} bean if no other bean of this type is present.
     *
     * @param openFgaClient the {@link OpenFgaClient} bean
     * @return the {@link OpenFga} bean
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenFga fga(OpenFgaClient openFgaClient) {
        return new OpenFga(openFgaClient);
    }
}
