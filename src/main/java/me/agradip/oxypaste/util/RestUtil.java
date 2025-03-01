package me.agradip.oxypaste.util;

import me.agradip.oxypaste.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class RestUtil {
    private RestUtil(){

    }


    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null; // Or throw an exception if you prefer
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;  // ✅ Safe cast
        }

        return null; // Or throw an exception if user isn't found
    }
}
