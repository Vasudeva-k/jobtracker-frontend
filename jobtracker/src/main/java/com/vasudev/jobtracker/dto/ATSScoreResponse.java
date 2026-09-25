package com.vasudev.jobtracker.dto;

import java.util.List;

public class ATSScoreResponse {

    private int atsScore;
    private String resumeStrength;
    private int keywordCoverage;
    private int readability;
    private int formatScore;
    private List<String> missingSections;
    private List<String> recommendations;
    private String source;

    public ATSScoreResponse() {
    }

    public ATSScoreResponse(
            int atsScore,
            String resumeStrength,
            int keywordCoverage,
            int readability,
            int formatScore,
            List<String> missingSections,
            List<String> recommendations) {

        this.atsScore = atsScore;
        this.resumeStrength = resumeStrength;
        this.keywordCoverage = keywordCoverage;
        this.readability = readability;
        this.formatScore = formatScore;
        this.missingSections = missingSections;
        this.recommendations = recommendations;
        this.source = "FALLBACK";
    }

    public ATSScoreResponse(
            int atsScore,
            String resumeStrength,
            int keywordCoverage,
            int readability,
            int formatScore,
            List<String> missingSections,
            List<String> recommendations,
            String source) {

        this.atsScore = atsScore;
        this.resumeStrength = resumeStrength;
        this.keywordCoverage = keywordCoverage;
        this.readability = readability;
        this.formatScore = formatScore;
        this.missingSections = missingSections;
        this.recommendations = recommendations;
        this.source = source;
    }

    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public String getResumeStrength() {
        return resumeStrength;
    }

    public void setResumeStrength(String resumeStrength) {
        this.resumeStrength = resumeStrength;
    }

    public int getKeywordCoverage() {
        return keywordCoverage;
    }

    public void setKeywordCoverage(int keywordCoverage) {
        this.keywordCoverage = keywordCoverage;
    }

    public int getReadability() {
        return readability;
    }

    public void setReadability(int readability) {
        this.readability = readability;
    }

    public int getFormatScore() {
        return formatScore;
    }

    public void setFormatScore(int formatScore) {
        this.formatScore = formatScore;
    }

    public List<String> getMissingSections() {
        return missingSections;
    }

    public void setMissingSections(List<String> missingSections) {
        this.missingSections = missingSections;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}