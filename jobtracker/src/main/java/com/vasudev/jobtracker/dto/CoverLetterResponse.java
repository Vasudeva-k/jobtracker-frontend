package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoverLetterResponse {

    private String coverLetter;
    private String source;

    public CoverLetterResponse(String coverLetter) {
        this.coverLetter = coverLetter;
        this.source = "FALLBACK";
    }
}