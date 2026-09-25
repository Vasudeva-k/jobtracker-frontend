package com.vasudev.jobtracker.dto;

import java.util.List;

public class JobSuccessPredictionResponse {

    private int successProbability;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> suggestions;
    private List<String> missingSkills;
    private String source;

    public JobSuccessPredictionResponse() {
    }

    public JobSuccessPredictionResponse(
            int successProbability,
            List<String> strengths,
            List<String> weaknesses,
            List<String> suggestions,
            List<String> missingSkills) {

        this.successProbability = successProbability;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.suggestions = suggestions;
        this.missingSkills = missingSkills;
        this.source = "FALLBACK";
    }

    public JobSuccessPredictionResponse(
            int successProbability,
            List<String> strengths,
            List<String> weaknesses,
            List<String> suggestions,
            List<String> missingSkills,
            String source) {

        this.successProbability = successProbability;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.suggestions = suggestions;
        this.missingSkills = missingSkills;
        this.source = source;
    }

    public int getSuccessProbability() {
        return successProbability;
    }

    public void setSuccessProbability(int successProbability) {
        this.successProbability = successProbability;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}