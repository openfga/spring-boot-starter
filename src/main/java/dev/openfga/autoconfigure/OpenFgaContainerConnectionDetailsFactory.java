package dev.openfga.autoconfigure;

import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.openfga.OpenFGAContainer;

/**
 * {@link ContainerConnectionDetailsFactory} that produces {@link OpenFgaConnectionDetails} for an
 * {@link OpenFGAContainer} annotated with
 * {@link org.springframework.boot.testcontainers.service.connection.ServiceConnection @ServiceConnection}.
 */
class OpenFgaContainerConnectionDetailsFactory
        extends ContainerConnectionDetailsFactory<OpenFGAContainer, OpenFgaConnectionDetails> {

    @Override
    protected OpenFgaConnectionDetails getContainerConnectionDetails(
            ContainerConnectionSource<OpenFGAContainer> source) {
        return new OpenFgaContainerConnectionDetails(source);
    }

    private static final class OpenFgaContainerConnectionDetails extends ContainerConnectionDetails<OpenFGAContainer>
            implements OpenFgaConnectionDetails {

        private OpenFgaContainerConnectionDetails(ContainerConnectionSource<OpenFGAContainer> source) {
            super(source);
        }

        @Override
        public String getApiUrl() {
            return getContainer().getHttpEndpoint();
        }
    }
}
