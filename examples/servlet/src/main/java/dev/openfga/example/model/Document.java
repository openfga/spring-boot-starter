package dev.openfga.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Document {

    private @Id String id;

    private String content;

    public Document() {}

    public Document(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Document{" + "id=" + this.id + ", content='" + this.content + "'}";
    }
}
