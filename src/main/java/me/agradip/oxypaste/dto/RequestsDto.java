package me.agradip.oxypaste.dto;

public class RequestsDto {
    public record PasteCreateRequest(String content, boolean isPublic) {}
}
