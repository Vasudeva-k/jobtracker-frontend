package com.vasudev.jobtracker.service;

import com.vasudev.jobtracker.dto.ResumePdfRequest;

public interface ResumePdfService {

    byte[] generateResumePdf(ResumePdfRequest request);

}
