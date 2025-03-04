package me.agradip.oxypaste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.agradip.oxypaste.model.Paste;

public class RequestsDto {
    public record PasteCreateRequest(String content, Paste.PasteVisibility visibility) {}
}
