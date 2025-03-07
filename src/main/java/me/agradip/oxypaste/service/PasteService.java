package me.agradip.oxypaste.service;

import me.agradip.oxypaste.config.AppConfig;
import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.model.User;
import me.agradip.oxypaste.repository.PasteRepository;
import me.agradip.oxypaste.util.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Paste createPaste(Paste paste) {
        return pasteRepository.save(paste);
    }

    public Optional<Paste> getPaste(String id) {
        return pasteRepository.findById(id);
    }

    public Page<Paste> getAllPastes(Pageable pageable) {
        return pasteRepository.findAll(pageable);
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
        paste.setContent(content);
        paste.setVisibility(Paste.PasteVisibility.PUBLIC);
        paste.setCreatedAt(creationTime);

        return paste;
    }
}