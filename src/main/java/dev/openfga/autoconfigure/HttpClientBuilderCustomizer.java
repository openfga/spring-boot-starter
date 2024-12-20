package dev.openfga.autoconfigure;

import java.net.http.HttpClient;

/**
 * Callback interface that can be used to customize the configuration of an {@link HttpClient.Builder}.
 */
@FunctionalInterface
public interface HttpClientBuilderCustomizer {

    /**
     * Callback to customize a {@link HttpClient.Builder} instance.
     * @param builder HTTP builder to customize
     */
    void customize(HttpClient.Builder builder);
}
