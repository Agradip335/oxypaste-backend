package me.agradip.oxypaste.service;

import me.agradip.oxypaste.dto.ResponsesDto;
import me.agradip.oxypaste.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

    @Value("${security.recaptcha-secret-key}")
    private String secretKey;

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public void verify(String recaptchaToken) {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secretKey);
        params.add("response", recaptchaToken);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<ResponsesDto.RecaptchaResponseDto> response =
                restTemplate.postForEntity(VERIFY_URL, request, ResponsesDto.RecaptchaResponseDto.class);

        ResponsesDto.RecaptchaResponseDto body = response.getBody();

        if (body == null || !body.success()) {
            throw new ApiException(400, "Failed reCAPTCHA verification");
        }
    }
}
