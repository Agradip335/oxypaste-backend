package me.agradip.oxypaste.service;

import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.repository.PasteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service  // Marks this as a business logic class
public class PasteService {

    private final PasteRepository pasteRepository;

    public PasteService(PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    public Paste createPaste(String content) {
        Paste paste = new Paste();
        paste.setContent(content);
        return pasteRepository.save(paste);  // Saves to DB
    }

    public Optional<Paste> getPaste(String id) {
        return pasteRepository.findById(id);
    }

    public List<Paste> getAllPastes() {
        return pasteRepository.findAll();
    }

    public void deletePaste(String id) {
        pasteRepository.delete(pasteRepository.getReferenceById(id));
    }
}
