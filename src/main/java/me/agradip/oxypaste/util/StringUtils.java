package me.agradip.oxypaste.util;

import java.util.HashMap;
import java.util.Map;

public class StringUtils {
    public static String replacePlaceholders(String content, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String escapedPlaceholder = "\\{\\{" + entry.getKey() + "\\}\\}";

            content = content.replaceAll(escapedPlaceholder, placeholder);

            content = content.replace(placeholder, entry.getValue());
        }
        return content;
    }
}
