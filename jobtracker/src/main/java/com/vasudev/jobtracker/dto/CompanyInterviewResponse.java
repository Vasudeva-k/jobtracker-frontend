package com.vasudev.jobtracker.dto;

import java.util.List;

public class CompanyInterviewResponse {

    private String company;
    private List<String> technicalQuestions;
    private List<String> hrQuestions;
    private List<String> codingTopics;
    private List<String> tips;
    private String source;

    public CompanyInterviewResponse() {
    }

    public CompanyInterviewResponse(
            String company,
            List<String> technicalQuestions,
            List<String> hrQuestions,
            List<String> codingTopics,
            List<String> tips) {

        this.company = company;
        this.technicalQuestions = technicalQuestions;
        this.hrQuestions = hrQuestions;
        this.codingTopics = codingTopics;
        this.tips = tips;
        this.source = "FALLBACK";
    }

    public CompanyInterviewResponse(
            String company,
            List<String> technicalQuestions,
            List<String> hrQuestions,
            List<String> codingTopics,
            List<String> tips,
            String source) {

        this.company = company;
        this.technicalQuestions = technicalQuestions;
        this.hrQuestions = hrQuestions;
        this.codingTopics = codingTopics;
        this.tips = tips;
        this.source = source;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public List<String> getTechnicalQuestions() {
        return technicalQuestions;
    }

    public void setTechnicalQuestions(List<String> technicalQuestions) {
        this.technicalQuestions = technicalQuestions;
    }

    public List<String> getHrQuestions() {
        return hrQuestions;
    }

    public void setHrQuestions(List<String> hrQuestions) {
        this.hrQuestions = hrQuestions;
    }

    public List<String> getCodingTopics() {
        return codingTopics;
    }

    public void setCodingTopics(List<String> codingTopics) {
        this.codingTopics = codingTopics;
    }

    public List<String> getTips() {
        return tips;
    }

    public void setTips(List<String> tips) {
        this.tips = tips;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}