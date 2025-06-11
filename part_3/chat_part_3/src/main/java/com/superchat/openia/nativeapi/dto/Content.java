package com.superchat.openia.nativeapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Content {
    private String type;

    // solo para type == "text"
    private String text;

    // solo para type == "image_url"
    @JsonProperty("image_url")
    private ImageUrl imageUrl;

    public static Content ofText(String text) {
        Content c = new Content();
        c.type = "text";
        c.text = text;
        return c;
    }

    public static Content ofImageBase64(String base64) {
        Content c = new Content();
        c.type = "image_url";
        ImageUrl img = new ImageUrl();
        img.setUrl("data:image/jpeg;base64," + base64);
        c.imageUrl = img;
        return c;
    }
}
