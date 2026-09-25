package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationResponse {

    private int matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String recommendation;
    private String source;

    public JobRecommendationResponse(int matchScore, List<String> matchedSkills, List<String> missingSkills, String recommendation) {
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills;
        this.missingSkills = missingSkills;
        this.recommendation = recommendation;
        this.source = "FALLBACK";
    }
}
