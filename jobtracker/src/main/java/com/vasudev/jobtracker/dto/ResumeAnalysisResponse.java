package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeAnalysisResponse {

    private List<String> summarySuggestions;

    private List<String> skillsSuggestions;

    private List<String> projectSuggestions;

    private List<String> experienceSuggestions;

    private List<String> educationSuggestions;

    private List<String> overallSuggestions;

}
