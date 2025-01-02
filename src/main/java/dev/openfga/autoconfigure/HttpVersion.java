package dev.openfga.autoconfigure;

import java.net.http.HttpClient;

/**
 * The HTTP protocol version to use for the client connection to the server when making requests to OpenFGA.
 */
public enum HttpVersion {

    /**
     * HTTP version 1.1
     * <p>
     * This version is widely supported and used for most HTTP communications.
     */
    HTTP_1_1(HttpClient.Version.HTTP_1_1),

    /**
     * HTTP version 2
     * <p>
     * This version offers improved performance and efficiency over HTTP/1.1.
     */
    HTTP_2(HttpClient.Version.HTTP_2);

    private final HttpClient.Version version;

    HttpVersion(HttpClient.Version version) {
        this.version = version;
    }

    HttpClient.Version getVersion() {
        return version;
    }
}
