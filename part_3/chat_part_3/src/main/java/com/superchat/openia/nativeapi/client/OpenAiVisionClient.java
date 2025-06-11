package com.superchat.openia.nativeapi.client;

import com.superchat.openia.nativeapi.dto.OpenAiVisionResponse;
import com.superchat.openia.nativeapi.dto.OpenAiVisionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "openaiClient", url = "https://api.openai.com/v1")
public interface OpenAiVisionClient {

    @PostMapping(value = "/chat/completions", consumes = MediaType.APPLICATION_JSON_VALUE)
    OpenAiVisionResponse analizarImagen(
            @RequestHeader("Authorization") String authorization,
            @RequestBody OpenAiVisionRequest request
    );
}
