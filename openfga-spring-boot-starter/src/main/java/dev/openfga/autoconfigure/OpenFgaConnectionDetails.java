package dev.openfga.autoconfigure;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface OpenFgaConnectionDetails extends ConnectionDetails {

    String getApiUrl();
}
