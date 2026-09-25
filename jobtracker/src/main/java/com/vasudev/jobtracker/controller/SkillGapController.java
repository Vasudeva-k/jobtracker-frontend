package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.dto.SkillGapResponse;
import com.vasudev.jobtracker.service.SkillGapService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skill-gap")
public class SkillGapController {

    private final SkillGapService skillGapService;

    public SkillGapController(
            SkillGapService skillGapService) {

        this.skillGapService = skillGapService;
    }

    @GetMapping
    public SkillGapResponse analyzeSkillGap(
            @RequestParam String targetRole,
            Authentication authentication) throws Exception {

        return skillGapService.analyzeSkillGap(
                targetRole,
                authentication.getName()
        );
    }
}