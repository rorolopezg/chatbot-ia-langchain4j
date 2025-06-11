package com.superchat.openia.nativeapi.dto;

import lombok.Data;

@Data
public class OpenAiVisionResponse {
    private Choice[] choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String content;
    }
}
