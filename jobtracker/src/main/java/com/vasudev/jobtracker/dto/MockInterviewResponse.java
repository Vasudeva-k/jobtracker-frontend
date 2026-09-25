package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MockInterviewResponse {

    private int score;
    private List<String> feedback;
    private List<String> strengths;
    private List<String> improvements;
    private String recommendation;
    private String source;

    public MockInterviewResponse(int score, List<String> feedback, List<String> strengths, List<String> improvements, String recommendation) {
        this.score = score;
        this.feedback = feedback;
        this.strengths = strengths;
        this.improvements = improvements;
        this.recommendation = recommendation;
        this.source = "FALLBACK";
    }
}
