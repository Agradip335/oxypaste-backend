package me.agradip.oxypaste.repository;

import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PasteRepository extends
        JpaRepository<Paste, String>,
        JpaSpecificationExecutor<Paste> {
    Page<Paste> findByVisibility(Paste.PasteVisibility visibility, Pageable pageable);

    List<Paste> findByTitleContainingIgnoreCaseAndVisibility(String title, Paste.PasteVisibility visibility, Pageable pageable);

    List<Paste> findByTitleContainingIgnoreCaseAndUser(String title, User user, Pageable pageable);

    List<Paste> findByTitleContainingIgnoreCaseAndUserAndVisibility(String title, User user, Paste.PasteVisibility visibility, Pageable pageable);

    List<Paste> findByContentContainingIgnoreCaseAndVisibility(String content, Paste.PasteVisibility visibility, Pageable pageable);

    List<Paste> findByContentContainingIgnoreCaseAndUser(String content, User user, Pageable pageable);

    List<Paste> findByContentContainingIgnoreCaseAndUserAndVisibility(String content, User user, Paste.PasteVisibility visibility, Pageable pageable);

    @Query("SELECT p FROM Paste p WHERE " +
            "(:keyword IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:visibility IS NULL OR p.visibility = :visibility) AND " +
            "(:createdAfter IS NULL OR p.createdAt >= :createdAfter) AND " +
            "(:createdBefore IS NULL OR p.createdAt <= :createdBefore)")
    Page<Paste> advancedSearch(
            @Param("keyword") String keyword,
            @Param("visibility") Paste.PasteVisibility visibility,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable
    );
}
