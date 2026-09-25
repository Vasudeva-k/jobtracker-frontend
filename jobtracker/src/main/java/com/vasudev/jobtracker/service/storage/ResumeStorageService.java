package com.vasudev.jobtracker.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ResumeStorageService {

    String saveFile(MultipartFile file) throws Exception;

    void deleteFile(String fileName) throws Exception;

}
