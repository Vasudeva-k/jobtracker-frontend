package com.vasudev.jobtracker.service;

public interface EmailService {

    void sendEmail(String to, String subject, String body);

}
