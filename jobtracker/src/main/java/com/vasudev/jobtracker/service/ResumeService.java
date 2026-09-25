package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.ResumeFileResponse;
import com.vasudev.jobtracker.dto.ResumeResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {

    ResumeResponse analyzeResume(
            MultipartFile file,
            String email
    ) throws Exception;

    ResumeFileResponse getMyResume(String email);

    byte[] downloadMyResume(String email) throws Exception;
}