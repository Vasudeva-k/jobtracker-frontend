package com.vasudev.jobtracker.dto;

import java.util.List;

public class JobMarketTrendResponse {

    private String role;
    private List<String> topSkills;
    private List<String> trendingTechnologies;
    private String averageSalary;
    private String jobDemand;
    private String futureScope;
    private String source;

    public JobMarketTrendResponse() {
    }

    public JobMarketTrendResponse(
            String role,
            List<String> topSkills,
            List<String> trendingTechnologies,
            String averageSalary,
            String jobDemand,
            String futureScope) {

        this.role = role;
        this.topSkills = topSkills;
        this.trendingTechnologies = trendingTechnologies;
        this.averageSalary = averageSalary;
        this.jobDemand = jobDemand;
        this.futureScope = futureScope;
        this.source = "FALLBACK";
    }

    public JobMarketTrendResponse(
            String role,
            List<String> topSkills,
            List<String> trendingTechnologies,
            String averageSalary,
            String jobDemand,
            String futureScope,
            String source) {

        this.role = role;
        this.topSkills = topSkills;
        this.trendingTechnologies = trendingTechnologies;
        this.averageSalary = averageSalary;
        this.jobDemand = jobDemand;
        this.futureScope = futureScope;
        this.source = source;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getTopSkills() {
        return topSkills;
    }

    public void setTopSkills(List<String> topSkills) {
        this.topSkills = topSkills;
    }

    public List<String> getTrendingTechnologies() {
        return trendingTechnologies;
    }

    public void setTrendingTechnologies(List<String> trendingTechnologies) {
        this.trendingTechnologies = trendingTechnologies;
    }

    public String getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(String averageSalary) {
        this.averageSalary = averageSalary;
    }

    public String getAverageSalaryRange() {
        return averageSalary;
    }

    public void setAverageSalaryRange(String averageSalaryRange) {
        this.averageSalary = averageSalaryRange;
    }

    public String getJobDemand() {
        return jobDemand;
    }

    public void setJobDemand(String jobDemand) {
        this.jobDemand = jobDemand;
    }

    public String getMarketDemand() {
        return jobDemand;
    }

    public void setMarketDemand(String marketDemand) {
        this.jobDemand = marketDemand;
    }

    public String getFutureScope() {
        return futureScope;
    }

    public void setFutureScope(String futureScope) {
        this.futureScope = futureScope;
    }

    public String getHiringGrowth() {
        return futureScope;
    }

    public void setHiringGrowth(String hiringGrowth) {
        this.futureScope = hiringGrowth;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
