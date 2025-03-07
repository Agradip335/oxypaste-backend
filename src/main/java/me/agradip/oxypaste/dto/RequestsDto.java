package me.agradip.oxypaste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.agradip.oxypaste.model.Paste;
import org.springframework.boot.context.properties.bind.DefaultValue;

public class RequestsDto {
    public record PasteCreateRequest(String content, @JsonProperty("public") @DefaultValue(value = "false") boolean isPublic) {}
}
