package com.vasudev.jobtracker.dto;

import java.util.List;

public class ResumeKeywordResponse {

    private String targetRole;
    private List<String> presentKeywords;
    private List<String> missingKeywords;
    private List<String> recommendedKeywords;
    private int optimizationScore;
    private String source;

    public ResumeKeywordResponse() {
    }

    public ResumeKeywordResponse(
            String targetRole,
            List<String> presentKeywords,
            List<String> missingKeywords,
            List<String> recommendedKeywords,
            int optimizationScore) {

        this.targetRole = targetRole;
        this.presentKeywords = presentKeywords;
        this.missingKeywords = missingKeywords;
        this.recommendedKeywords = recommendedKeywords;
        this.optimizationScore = optimizationScore;
        this.source = "FALLBACK";
    }

    public ResumeKeywordResponse(
            String targetRole,
            List<String> presentKeywords,
            List<String> missingKeywords,
            List<String> recommendedKeywords,
            int optimizationScore,
            String source) {

        this.targetRole = targetRole;
        this.presentKeywords = presentKeywords;
        this.missingKeywords = missingKeywords;
        this.recommendedKeywords = recommendedKeywords;
        this.optimizationScore = optimizationScore;
        this.source = source;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public List<String> getPresentKeywords() {
        return presentKeywords;
    }

    public void setPresentKeywords(List<String> presentKeywords) {
        this.presentKeywords = presentKeywords;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public List<String> getRecommendedKeywords() {
        return recommendedKeywords;
    }

    public void setRecommendedKeywords(List<String> recommendedKeywords) {
        this.recommendedKeywords = recommendedKeywords;
    }

    public int getOptimizationScore() {
        return optimizationScore;
    }

    public void setOptimizationScore(int optimizationScore) {
        this.optimizationScore = optimizationScore;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
