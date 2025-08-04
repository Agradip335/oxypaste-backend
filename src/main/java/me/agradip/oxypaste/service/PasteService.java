package me.agradip.oxypaste.service;

import me.agradip.oxypaste.config.AppConfig;
import me.agradip.oxypaste.exception.ApiException;
import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.repository.PasteRepository;
import me.agradip.oxypaste.spec.PasteSpecifications;
import me.agradip.oxypaste.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Service
@EnableCaching
public class PasteService {

    @Autowired
    private AppConfig appConfig;

    private final PasteRepository pasteRepository;

    @Autowired
    public PasteService(PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    public long countPastes() { return pasteRepository.count(); }

    public Paste createPaste(Paste paste) {
        return pasteRepository.save(paste);
    }

    public Optional<Paste> getPaste(String id) {
        return pasteRepository.findById(id);
    }

    public Page<Paste> getAllPastes(Pageable pageable) {
        return pasteRepository.findAll(pageable);
    }

    public List<Paste> searchPublicPastes(
            String keyword,
            Pageable pageable,
            boolean searchTitleOnly,
            Paste.PasteVisibility visibility,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    ) {
        Specification<Paste> spec = Specification
                .where(PasteSpecifications.containsKeyword(keyword, searchTitleOnly))
                .and(PasteSpecifications.hasVisibility(Paste.PasteVisibility.PUBLIC));

        if (visibility != null) {
            spec = spec.and(PasteSpecifications.hasVisibility(visibility));
        }

        if (createdAfter != null) {
            spec = spec.and(PasteSpecifications.createdAfter(createdAfter));
        }

        if (createdBefore != null) {
            spec = spec.and(PasteSpecifications.createdBefore(createdBefore));
        }

        return pasteRepository.findAll(spec, pageable).getContent();
    }

    public List<Paste> searchUserPastes(
            String keyword,
            User user,
            Pageable pageable,
            boolean searchTitleOnly,
            Paste.PasteVisibility visibility,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    ) {
        Specification<Paste> spec = Specification
                .where(PasteSpecifications.containsKeyword(keyword, searchTitleOnly))
                .and(PasteSpecifications.createdBy(user));

        if (visibility != null) {
            spec = spec.and(PasteSpecifications.hasVisibility(visibility));
        }

        if (createdAfter != null) {
            spec = spec.and(PasteSpecifications.createdAfter(createdAfter));
        }

        if (createdBefore != null) {
            spec = spec.and(PasteSpecifications.createdBefore(createdBefore));
        }

        return pasteRepository.findAll(spec, pageable).getContent();
    }

    public List<Paste> searchPublicPastesByUser(
            String keyword,
            User user,
            Pageable pageable,
            boolean searchTitleOnly,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore
    ) {
        Specification<Paste> spec = Specification
                .where(PasteSpecifications.containsKeyword(keyword, searchTitleOnly))
                .and(PasteSpecifications.createdBy(user))
                .and(PasteSpecifications.hasVisibility(Paste.PasteVisibility.PUBLIC));

        if (createdAfter != null) {
            spec = spec.and(PasteSpecifications.createdAfter(createdAfter));
        }

        if (createdBefore != null) {
            spec = spec.and(PasteSpecifications.createdBefore(createdBefore));
        }

        return pasteRepository.findAll(spec, pageable).getContent();
    }

    public void deletePaste(String id) {
        pasteRepository.delete(pasteRepository.getReferenceById(id));
    }

    public Page<Paste> getPublicPastes(Pageable pageable) {
        return pasteRepository.findByVisibility(Paste.PasteVisibility.PUBLIC, pageable);
    }

    @Cacheable(value = "rootDocumentsCache", key = "#key")
    public Paste getRootDocument(String key) {
        String filePath = appConfig.getDocuments().get(key);
        if (filePath == null) throw new IllegalArgumentException("Root document with key " + key + " not found.");

        String content = IOUtil.readFileSync(new File(filePath));
        LocalDateTime creationTime = IOUtil.getFileCreationTime(filePath);

        Paste paste = new Paste(key);
        paste.setTitle(key); // todo fix this later
        paste.setContent(content);
        paste.setVisibility(Paste.PasteVisibility.PUBLIC);
        paste.setCreatedAt(creationTime);

        return paste;
    }
}