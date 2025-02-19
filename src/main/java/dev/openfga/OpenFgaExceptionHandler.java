package dev.openfga;

import static java.util.Objects.requireNonNull;

import dev.openfga.sdk.errors.FgaError;
import java.util.Objects;

/**
 * The OpenFgaExceptionHandler class provides centralized exception handling for OpenFGA application logic.
 * It is responsible for processing exceptions, extracting meaningful details, and creating custom runtime
 * exceptions tailored to the OpenFGA domain.
 * <br/>
 * This handler ensures that interrupted threads are re-interrupted
 * and wraps exceptions in a meaningful and consistent representation.
 * <br/>
 * The class aims to:
 * <ul>
 * <li>Handle thread interruptions by propagating the interrupt status.</li>
 * <li>Capture root causes of exceptions for detailed error reporting.</li>
 * <li>Create domain-specific exceptions, such as {@link OpenFgaException}, encapsulating details
 *   relevant to OpenFGA operations.</li>
 * </ul>
 * <br/>
 * This class is intended for internal use within the OpenFGA system to standardize exception management.
 */
public class OpenFgaExceptionHandler {
    /**
     * Handles a given throwable cause and message, processes interruption if applicable,
     * and transforms the input parameters into an OpenFGA-specific exception.
     *
     * @param cause the throwable instance representing the root cause of the error
     * @param message the error message describing the context or details of the exception
     * @param args optional arguments to format the error message
     *
     * @return an instance of {@link OpenFgaException} with detailed contextual information
     */
    public OpenFgaException handle(final Throwable cause, final String message, final Object... args) {
        if (cause instanceof InterruptedException) {
            interruptThread();
        }
        return toDetailedException(cause, message, args);
    }

    private static void interruptThread() {
        Thread.currentThread().interrupt();
    }

    private OpenFgaException toDetailedException(final Throwable cause, final String message, final Object... args) {
        final var messageFormatted = String.format(message, args);
        return details(messageFormatted, cause);
    }

    private OpenFgaException details(final String message, final Throwable cause) {
        final var rootCause = getRootCause(cause);
        if (!(rootCause instanceof FgaError error)) {
            return new OpenFgaException(message, cause, null);
        }

        return new OpenFgaException(message, cause, error);
    }

    private static Throwable getRootCause(Throwable throwable) {
        var rootCause = requireNonNull(throwable);
        while ((rootCause.getCause() != null) && !Objects.equals(rootCause.getCause(), rootCause)) {
            rootCause = rootCause.getCause();
        }

        return rootCause;
    }
}
