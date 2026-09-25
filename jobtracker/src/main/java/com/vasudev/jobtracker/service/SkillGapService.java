package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.SkillGapResponse;

public interface SkillGapService {

    SkillGapResponse analyzeSkillGap(
            String targetRole,
            String email
    ) throws Exception;
}
