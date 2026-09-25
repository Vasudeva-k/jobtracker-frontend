package com.vasudev.jobtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CareerRoadmapResponse {

    private String targetRole;
    private List<String> roadmap;
    private String source;

    public CareerRoadmapResponse(String targetRole, List<String> roadmap) {
        this.targetRole = targetRole;
        this.roadmap = roadmap;
        this.source = "FALLBACK";
    }
}
