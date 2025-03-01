package me.agradip.oxypaste.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class IOUtil {
    public static String readFileSync(File file) {
        return readFileSync(file.toPath());
    }

    public static String readFileSync(String path) {
        return readFileSync(Path.of(path));
    }

    public static String readFileSync(Path path) {
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    public static LocalDateTime getFileCreationTime(String filePath) {
        try {
            return LocalDateTime.ofInstant(
                    Files.readAttributes(Path.of(filePath), BasicFileAttributes.class)
                            .creationTime()
                            .toInstant(),
                    ZoneId.systemDefault()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file attributes for " + filePath, e);
        }
    }

    public static String readResourceFromCp(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }

}
