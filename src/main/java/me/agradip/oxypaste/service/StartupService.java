package me.agradip.oxypaste.service;


import me.agradip.oxypaste.util.FileExtractor;
import me.agradip.oxypaste.util.IOUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class StartupService {

    private final FileExtractor fileExtractor;

    public StartupService(FileExtractor fileExtractor) {
        this.fileExtractor = fileExtractor;
    }

    @PostConstruct
    public void init() throws IOException {
        Map<String, String> extractionMap = getExtractionMap();

        for (Map.Entry<String, String> entry : extractionMap.entrySet()) {
            String classpathFile = entry.getKey();
            String destinationFile = entry.getValue();

            File targetFile = new File(destinationFile);

            // Ensure target directory exists
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    throw new IOException("Failed to create directories: " + parentDir.getAbsolutePath());
                }
            }

            // Skip extraction if the file already exists
            if (targetFile.exists()) continue;

            fileExtractor.extractFile(classpathFile, destinationFile);
        }
    }

    private Map<String, String> getExtractionMap() throws IOException {
        Map<String, String> extractionMap = new HashMap<>();

        // Read file as a single string
        String content = IOUtil.readResourceFromCp("extraction.txt");
        if (content == null || content.isBlank()) {
            throw new IOException("extraction.txt is empty or not found in classpath");
        }

        // Process each line
        String[] lines = content.split("\\R"); // Splits on any line break (\n or \r\n)
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // Skip empty lines and comments
            }

            String[] parts = line.split(":", 2);
            if (parts.length == 2) {
                extractionMap.put(parts[0].trim(), parts[1].trim());
            } else {
                throw new IOException("Invalid format in extraction.txt: " + line);
            }
        }

        return extractionMap;
    }
}
