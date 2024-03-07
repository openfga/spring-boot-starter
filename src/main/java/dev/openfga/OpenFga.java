package dev.openfga;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientCheckRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.util.concurrent.ExecutionException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// TODO what package?
@Component
public class OpenFga {

    private final OpenFgaClient fgaClient;

    // Inject OpenFga client
    public OpenFga(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    /**
     * Perform an FGA check. The user ID will be obtained from the authentication name in the {@link org.springframework.security.core.context.SecurityContext}
     *
     * @param objectType
     * @param objectId
     * @param relation
     * @param userType
     * @return true if the user has the required relation to the object, false otherwise
     */
    public boolean check(String objectType, String objectId, String relation, String userType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException(
                    "No user provided, and no authentication could be found in the security context");
        }
        return check(objectId, objectType, relation, userType, authentication.getName());
    }

    /**
     * Perform an FGA check.
     *
     * @param objectType
     * @param objectId
     * @param relation
     * @param userType
     * @param userId
     * @return true if the user has the required relation to the object, false otherwise
     */
    public boolean check(String objectType, String objectId, String relation, String userType, String userId) {
        var body = new ClientCheckRequest()
                .user(String.format("%s:%s", userType, userId))
                .relation(relation)
                ._object(String.format("%s:%s", objectType, objectId));

        try {
            return Boolean.TRUE.equals(fgaClient.check(body).get().getAllowed());
        } catch (InterruptedException | FgaInvalidParameterException | ExecutionException e) {
            throw new RuntimeException("Error performing FGA check", e);
        }
    }
}
