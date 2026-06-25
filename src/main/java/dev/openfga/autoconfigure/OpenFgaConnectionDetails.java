package dev.openfga.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

/**
 * Details required to connect to an OpenFGA instance.
 *
 * <p>By default these are sourced from the {@code openfga.*} configuration properties, but a
 * {@link ConnectionDetails} bean (for example, one contributed by a Testcontainers
 * {@code @ServiceConnection}) takes precedence when present.
 */
public interface OpenFgaConnectionDetails extends ConnectionDetails {

    /**
     * Returns the URL of the OpenFGA instance.
     *
     * @return the API URL
     */
    String getApiUrl();
}
