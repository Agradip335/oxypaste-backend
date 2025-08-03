package me.agradip.oxypaste.spec;

import me.agradip.oxypaste.model.Paste;
import me.agradip.oxypaste.model.User;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class PasteSpecifications {

    public static Specification<Paste> containsKeyword(String keyword, boolean searchTitle) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            if (searchTitle) {
                return cb.like(cb.lower(root.get("title")), pattern);
            } else {
                return cb.like(cb.lower(root.get("content")), pattern);
            }
        };
    }

    public static Specification<Paste> hasVisibility(Paste.PasteVisibility visibility) {
        return (root, query, cb) -> cb.equal(root.get("visibility"), visibility);
    }

    public static Specification<Paste> createdAfter(LocalDateTime time) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), time);
    }

    public static Specification<Paste> createdBefore(LocalDateTime time) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), time);
    }

    public static Specification<Paste> createdBy(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }
}
