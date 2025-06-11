package com.superchat.openia.nativeapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class Message {
    private String role;
    private List<Content> content;
}