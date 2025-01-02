package dev.openfga.autoconfigure;

import dev.openfga.sdk.telemetry.Counters;
import dev.openfga.sdk.telemetry.Histograms;
import dev.openfga.sdk.telemetry.Metric;
import java.util.Objects;

/**
 * The telemetry metrics that can be used to log telemetry data.
 *
 * See <a href="https://openfga.dev/docs/getting-started/configure-telemetry">OpenFGA Telemetry</a> for more information.
 */
public enum TelemetryMetric {
    /**
     * The CREDENTIALS_REQUEST counter represents the number of times an access token is requested.
     */
    CREDENTIALS_REQUEST(Counters.CREDENTIALS_REQUEST),
    /**
     * A histogram for measuring the total time it took (in milliseconds) for the FGA server to process and evaluate the request.
     */
    QUERY_DURATION(Histograms.QUERY_DURATION),
    /**
     * A histogram for measuring the total time (in milliseconds) it took for the request to complete, including the time it took to send the request and receive the response.
     */
    REQUEST_DURATION(Histograms.REQUEST_DURATION);

    private final Metric metric;

    TelemetryMetric(Metric metric) {
        this.metric = Objects.requireNonNull(metric);
    }

    Metric getMetric() {
        return metric;
    }
}
