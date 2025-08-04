package me.agradip.oxypaste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import me.agradip.oxypaste.model.Paste;
import org.springframework.boot.context.properties.bind.DefaultValue;

public class RequestsDto {
    public record PasteCreateRequest(
            @Schema(description = "Optional title of your paste", nullable = true)
            String title,

            @Schema(description = "The content of your paste")
            String content,

            @JsonProperty("public")
            @DefaultValue(value = "false")
            @Schema(description = "If the paste is public or not", defaultValue = "false", nullable = true)
            boolean isPublic,

            @DefaultValue(value = "")
            @Schema(description = "The language of the paste", defaultValue = "", nullable = true)
            String language
    ) {}
}
