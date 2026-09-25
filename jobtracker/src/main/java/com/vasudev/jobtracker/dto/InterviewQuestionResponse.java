package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestionResponse {

    private String jobRole;
    private List<String> questions;
    private String source;

    public InterviewQuestionResponse(String jobRole, List<String> questions) {
        this.jobRole = jobRole;
        this.questions = questions;
        this.source = "FALLBACK";
    }

    public InterviewQuestionResponse(List<String> questions) {
        this.questions = questions;
        this.source = "FALLBACK";
    }

    public InterviewQuestionResponse(List<String> questions, String source) {
        this.questions = questions;
        this.source = source;
    }
}