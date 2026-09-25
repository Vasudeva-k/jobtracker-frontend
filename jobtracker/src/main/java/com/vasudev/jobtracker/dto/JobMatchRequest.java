package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchRequest {

    // Resume text extracted from PDF
    private String resumeText;

    // Job description pasted by the user
    private String jobDescription;
}
