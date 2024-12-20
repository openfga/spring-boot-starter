package dev.openfga;

/**
 * Exception thrown when there is an error during an OpenFGA check operation.
 * This exception is typically used to wrap lower-level exceptions and provide
 * additional context about the failure.
 *
 * See the {@link OpenFga#check(String, String, String, String, String)} method for more information about the check operation.
 *
 */
public class OpenFgaCheckException extends RuntimeException {

    /**
     * Constructs a new OpenFgaCheckException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the underlying cause of the exception
     */
    public OpenFgaCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
