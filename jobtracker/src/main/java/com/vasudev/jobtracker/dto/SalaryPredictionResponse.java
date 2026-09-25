package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPredictionResponse {

    private String estimatedSalary;
    private String experienceLevel;
    private String marketDemand;
    private List<String> suggestions;
    private String source;

    public SalaryPredictionResponse(String estimatedSalary, String experienceLevel, String marketDemand, List<String> suggestions) {
        this.estimatedSalary = estimatedSalary;
        this.experienceLevel = experienceLevel;
        this.marketDemand = marketDemand;
        this.suggestions = suggestions;
        this.source = "FALLBACK";
    }
}
