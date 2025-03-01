package me.agradip.oxypaste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestsDto {
    public record PasteCreateRequest(String content, @JsonProperty("public") boolean isPublic) {}
}
