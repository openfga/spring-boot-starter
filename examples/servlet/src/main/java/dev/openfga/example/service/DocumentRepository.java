package dev.openfga.example.service;

import dev.openfga.example.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, String> {}
