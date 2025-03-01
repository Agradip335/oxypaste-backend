package me.agradip.oxypaste.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileExtractor {

    private final ResourceLoader resourceLoader;

    public FileExtractor(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void extractFile(String classpathLocation, String destinationPath) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + classpathLocation);

        if (!resource.exists()) {
            throw new FileNotFoundException("File not found in classpath: " + classpathLocation);
        }

        try (InputStream inputStream = resource.getInputStream()) {
            Files.copy(inputStream, Path.of(destinationPath));
        }
    }
}

