package dev.openfga.example.controllers;

import dev.openfga.example.model.Document;
import dev.openfga.example.service.DocumentService;
import dev.openfga.sdk.api.model.Tuple;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.*;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents/{id}")
    public Optional<Document> getDocument(@PathVariable String id) {
        return documentService.getDocumentWithPreAuthorize(id);
    }

    @PostMapping("/documents")
    public Document createDocument(@RequestBody Document document) {
        return documentService.createDoc(document);
    }

    /**
     * For convenience only; lists the authorization tuples.
     *
     * @return authorization tuples
     */
    @GetMapping("/tuples")
    public List<Tuple> getTuples() throws Exception {
        return documentService.getTuples().getTuples();
    }
}
