package dev.openfga;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.util.concurrent.ExecutionException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * A bean that can be used to perform common FGA tasks, such as execute a check request.<br/>
 *
 * Can be used in {@code @PreAuthorize} or {@code PostAuthorize} to provide method-level FGA
 * protection for a requested resource.<br/><br/>For example:
 * <pre>
 * {@code @PreAuthorize("@openFga.check('document', #id, 'reader', 'user', 'authentication?.name')")
 * public Document getDocument(String id) {
 *     repository.findById(id);
 * }}
 * </pre>
 */
public class OpenFga {

    private final OpenFgaClient fgaClient;
    private final OpenFgaExceptionHandler exceptionHandler;

    /**
     * Create a new OpenFGA instance.
     *
     * @param fgaClient The {@link OpenFgaClient} to use
     * @param exceptionHandler The {@link OpenFgaExceptionHandler} to use
     */
    public OpenFga(OpenFgaClient fgaClient, OpenFgaExceptionHandler exceptionHandler) {
        this.fgaClient = fgaClient;
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Perform an FGA check. Returns {@code true} if the user has the specified relationship with the object, {@code false}
     * otherwise. The user ID will be obtained from the authentication name in the {@link org.springframework.security.core.context.SecurityContext}
     *
     * @param objectType The object type of the check
     * @param objectId The ID of the object to check
     * @param relation The required relation between the user and the object
     * @param userType The type of the user
     * @return true if the user has the required relation to the object, false otherwise
     *
     * @see <a href="https://openfga.dev/api/service#/Relationship%20Queries/Check">FGA Check API</a>
     */
    public boolean check(String objectType, String objectId, String relation, String userType) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException(
                    "No user provided, and no authentication could be found in the security context");
        }
        return check(objectType, objectId, relation, userType, authentication.getName());
    }

    /**
     * Perform an FGA check. Returns {@code true} if the user has the specified relationship with the object, {@code false}
     * otherwise. The user ID will be obtained from the authentication name in the {@link org.springframework.security.core.context.SecurityContext}
     *
     * @param objectType The object type of the check
     * @param objectId The ID of the object to check
     * @param relation The required relation between the user and the object
     * @param userType The type of the user
     * @param userId The ID of the user
     * @return true if the user has the required relation to the object, false otherwise
     *
     * @see <a href="https://openfga.dev/api/service#/Relationship%20Queries/Check">FGA Check API</a>
     */
    public boolean check(String objectType, String objectId, String relation, String userType, String userId) {
        var body = new ClientCheckRequest()
                .user(String.format("%s:%s", userType, userId))
                .relation(relation)
                ._object(String.format("%s:%s", objectType, objectId));
        try {
            return Boolean.TRUE.equals(fgaClient.check(body).get().getAllowed());
        } catch (InterruptedException | FgaInvalidParameterException | ExecutionException cause) {
            throw exceptionHandler.handle(cause, "Error performing FGA check");
        }
    }
}
