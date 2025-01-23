package dev.openfga;

import static java.util.Objects.nonNull;

import dev.openfga.sdk.errors.FgaError;
import java.io.Serial;
import org.springframework.core.style.ToStringCreator;

/**
 * Represents a custom runtime exception in the OpenFGA domain.
 * <br/>
 * The `OpenFgaException` class extends {@link RuntimeException} and serves as a specialized exception
 * used throughout the OpenFGA application to handle errors associated with FGA operations.
 * This exception encapsulates additional contextual information provided by the {@link FgaError} object.
 * <br/>
 * Key Features:
 * <ul>
 * <li>Stores an instance of {@link FgaError}, which contains detailed error information, such as status codes, API details,
 *   and response data from failed FGA operations.</li>
 * <li>Provides an enhanced implementation of the `toString` method, which includes both the standard error properties
 *   (message and cause) and the contextual error details from the associated {@link FgaError}.</li>
 * <li>Allows retrieval of the associated {@link FgaError} object for further processing or debugging through the
 *   `getFgaError` method.</li>
 * </ul>
 * This exception is typically created and thrown by the {@link OpenFgaExceptionHandler}
 * to ensure consistent error management in the OpenFGA starter.
 */
public class OpenFgaException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5956276395488544542L;

    /**
     * Represents an immutable {@link FgaError} object that provides context-specific details
     * about an error encountered during OpenFGA operations.
     * <br/>
     * This field is used to encapsulate error information such as status codes, API request metadata,
     * or additional debugging data. It is assigned during the construction of an {@link OpenFgaException}
     * instance and can be retrieved using the respective getter method.
     * <br/>
     * It aids in delivering detailed error diagnostics and maintaining consistency in error reporting
     * within the OpenFGA domain.
     */
    private final FgaError fgaError;

    /**
     * Constructs a new instance of the OpenFgaException.
     *
     * @param message The detail message associated with the exception.
     * @param cause The root cause of the exception (can be null).
     * @param fgaError An {@link FgaError} object containing contextual details about the error.
     */
    public OpenFgaException(String message, Throwable cause, FgaError fgaError) {
        super(message, cause);
        this.fgaError = fgaError;
    }

    /**
     * Retrieves the {@link FgaError} object associated with this exception.
     *
     * @return The {@link FgaError} containing context-specific error details, such as status codes,
     *         API request metadata, or additional debugging information.
     */
    public FgaError getFgaError() {
        return fgaError;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public String toString() {
        var toStringCreator = new ToStringCreator(this);
        toStringCreator.append("message", getMessage()).append("cause", getCause());
        if (nonNull(getFgaError())) {
            toStringCreator
                    .append("statusCode", getFgaError().getStatusCode())
                    .append("method", getFgaError().getMethod())
                    .append("requestUrl", getFgaError().getRequestUrl())
                    .append("apiErrorCode", getFgaError().getApiErrorCode())
                    .append("audience", getFgaError().getAudience())
                    .append("grantType", getFgaError().getGrantType())
                    .append("clientId", getFgaError().getClientId())
                    .append("apiErrorCode", getFgaError().getRequestId())
                    .append("responseData", getFgaError().getResponseData());
        }

        return toStringCreator.toString();
    }
}
