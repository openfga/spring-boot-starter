package dev.openfga.example.service;

import dev.openfga.example.model.Document;
import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.client.model.ClientReadRequest;
import dev.openfga.sdk.api.client.model.ClientReadResponse;
import dev.openfga.sdk.api.client.model.ClientTupleKey;
import dev.openfga.sdk.api.client.model.ClientWriteRequest;
import dev.openfga.sdk.errors.FgaInvalidParameterException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private final OpenFgaClient fgaClient;
    private final DocumentRepository repository;

    public DocumentService(OpenFgaClient fgaClient, DocumentRepository repository) {
        this.fgaClient = fgaClient;
        this.repository = repository;
    }

    /**
     * Ensure user has permission in PreAuthorize
     */
    @PreAuthorize("@fga.check('document', #id, 'can_read', 'user', 'anne')")
    public Optional<Document> getDocumentWithPreAuthorize(String id) {
        return repository.findById(id);
    }

    public ClientReadResponse getTuples() throws Exception {
        return fgaClient.read(new ClientReadRequest()).get();
    }

    /**
     * Demonstrates a simple example of using the injected fgaClient to write authorization data to FGA.
     */
    public Document createDoc(Document document) {

        // write to fga
        ClientWriteRequest writeRequest = new ClientWriteRequest()
                .writes(List.of(new ClientTupleKey()
                        .user("user:anne")
                        .relation("owner")
                        ._object(String.format("document:%s", document.getId()))));

        try {
            fgaClient.write(writeRequest).get();
        } catch (InterruptedException | ExecutionException | FgaInvalidParameterException e) {
            throw new RuntimeException("Error writing to FGA", e);
        }

        // create doc
        return repository.save(document);
    }
}
