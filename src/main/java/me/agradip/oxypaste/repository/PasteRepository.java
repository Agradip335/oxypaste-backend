package me.agradip.oxypaste.repository;

import me.agradip.oxypaste.model.Paste;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasteRepository extends JpaRepository<Paste, String> {
    Page<Paste> findByVisibility(Paste.PasteVisibility visibility, Pageable pageable);
}
