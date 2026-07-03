package dev.openfga.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ClientConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.openfga.OpenFGAContainer;

/**
 * Verifies that a Testcontainers {@code @ServiceConnection} OpenFGA container is auto-mapped into
 * the Spring environment and consumed by {@link OpenFgaAutoConfiguration}.
 *
 * <p>The whole class is skipped when Docker is unavailable, so it does not hard-fail in CI
 * environments without a Docker daemon.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OpenFgaServiceConnectionTests {

    @Container
    @ServiceConnection
    static OpenFGAContainer openfga = new OpenFGAContainer("openfga/openfga:v1.4.3");

    @Autowired
    OpenFgaConnectionDetails connectionDetails;

    @Autowired
    ClientConfiguration clientConfiguration;

    @Autowired
    OpenFgaClient openFgaClient;

    @Test
    void connectionDetailsAreResolvedFromContainer() {
        assertThat(connectionDetails.getApiUrl()).isEqualTo(openfga.getHttpEndpoint());
        assertThat(clientConfiguration.getApiUrl()).isEqualTo(openfga.getHttpEndpoint());
        assertThat(openFgaClient).isNotNull();
    }

    @SpringBootConfiguration
    @ImportAutoConfiguration(OpenFgaAutoConfiguration.class)
    static class TestConfig {}
}
