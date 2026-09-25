package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String response;
    private String source;

    public ChatResponse(String response) {
        this.response = response;
        this.source = "FALLBACK";
    }

    public String getMessage() {
        return response;
    }

    public void setMessage(String message) {
        this.response = message;
    }
}
