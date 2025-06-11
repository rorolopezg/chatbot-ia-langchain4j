package com.superchat.openia.nativeapi.dto;

import lombok.Data;
import java.util.List;

@Data
public class OpenAiVisionRequest {
    private String model;
    private Double temperature;
    private List<Message> messages;
}
