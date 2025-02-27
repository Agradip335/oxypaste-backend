package me.agradip.oxypaste;

import java.util.Random;

public class Utils {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+";


    public static String generateRandom(int len) {
        if (len <= 0) {
            throw new IllegalArgumentException("Length must be a positive integer.");
        }

        Random random = new Random();
        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            // Randomly pick a character from the CHARACTERS string
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
