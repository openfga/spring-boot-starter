package dev.openfga.example;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents/{id}")
    public Optional<Document> getDocument(@PathVariable String id) {
        return documentService.getDocument(id);
    }

    @PostMapping("/documents")
    public Document createDocument(@RequestBody Document document) {
        return documentService.saveDocumentWithFga(document);
    }
}
