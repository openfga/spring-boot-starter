package dev.openfga.example;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class DocumentService {

    private final Set<Document> documentSet = new HashSet<>();
    private final OpenFgaClient fgaClient;

    public DocumentService(OpenFgaClient fgaClient) {
        this.fgaClient = fgaClient;
    }

    /**
     * Return the requested document, if it exists, only if the user has the
     * required FGA relationship.
     *
     * Uses the currently authenticated principal as the user ID for the FGA check.
     *
     * @param id The ID of the document to get.
     * @return the document.
     */
    @PreAuthorize("@fga.check('document', #id, 'can_read', 'user')")
    public Optional<Document> getDocument(String id) {
        return documentSet.stream()
                .filter(d -> d.id().equals(id))
                .findFirst();
    }

    /**
     * Save document and write to FGA associating the principal (hard-coded to 'anne' in this examnple)
     * as having the owner relationship to the document.
     *
     * @param document the document to create.
     * @return the created document.
     */
    public Document saveDocumentWithFga(Document document) {
        // write to fga
        ClientWriteRequest writeRequest = new ClientWriteRequest()
                .writes(List.of(new ClientTupleKey()
                        .user("user:anne")
                        .relation("owner")
                        ._object(String.format("document:%s", document.id()))));

        try {
            fgaClient.write(writeRequest).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException("Error writing to FGA", e);
        }

        // create doc
        return saveDocument(document);
    }

    /**
     * Save a document without writing an FGA data.
     *
     * @param document the document to save.
     * @return the saved document.
     */
    public Document saveDocument(Document document) {
        documentSet.add(document);
        return document;
    }
}
