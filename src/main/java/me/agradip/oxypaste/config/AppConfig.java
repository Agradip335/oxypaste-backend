package me.agradip.oxypaste.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@ConfigurationProperties(prefix = "application")
public class AppConfig {

    private Map<String, String> placeholders = Map.of();
    private Map<String, String> documents = Map.of();

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    public void setPlaceholders(Map<String, String> placeholders) {
        this.placeholders = placeholders;
    }

    public Map<String, String> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<String, String> documents) {
        this.documents = documents;
    }


    public static class Document {
        private String path;

        public Document(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
