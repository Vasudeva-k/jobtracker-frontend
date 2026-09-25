package com.vasudev.jobtracker.controller;

import com.vasudev.jobtracker.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/api/email/test")
    public String sendTestEmail() {

        emailService.sendEmail(
                "vasudeva.katasani@gmail.com",
                "Job Tracker Test Email",
                "Congratulations! Your Email Notification System is working successfully."
        );

        return "Email Sent Successfully!";
    }
}