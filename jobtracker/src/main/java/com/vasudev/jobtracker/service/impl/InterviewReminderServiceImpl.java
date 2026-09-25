package com.vasudev.jobtracker.service.impl;

import com.vasudev.jobtracker.entity.JobApplication;
import com.vasudev.jobtracker.repository.JobApplicationRepository;
import com.vasudev.jobtracker.service.EmailService;
import com.vasudev.jobtracker.service.InterviewReminderService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InterviewReminderServiceImpl implements InterviewReminderService {

    private final JobApplicationRepository jobApplicationRepository;
    private final EmailService emailService;
    private final Set<String> sentReminderKeys = ConcurrentHashMap.newKeySet();

    public InterviewReminderServiceImpl(
            JobApplicationRepository jobApplicationRepository,
            EmailService emailService) {

        this.jobApplicationRepository = jobApplicationRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void sendTodayInterviewReminders() {

        List<JobApplication> interviews =
                jobApplicationRepository.findTodayInterviews();

        if (interviews.isEmpty()) {
            System.out.println("No interviews scheduled for today.");
            return;
        }

        LocalDate today = LocalDate.now();

        for (JobApplication job : interviews) {

            if (job.getUser() == null || job.getUser().getEmail() == null) {
                continue;
            }

            String reminderKey = job.getId() + "_" + today;
            if (sentReminderKeys.contains(reminderKey)) {
                continue;
            }

            String email = job.getUser().getEmail();
            String name = job.getUser().getFirstName() != null ? job.getUser().getFirstName() : "Job Seeker";

            String subject = "Interview Reminder: " + (job.getCompanyName() != null ? job.getCompanyName() : "Upcoming Interview");

            String body =
                    "Hello " + name + ",\n\n" +
                            "This is a reminder that you have an interview scheduled today.\n\n" +
                            "Company : " + (job.getCompanyName() != null ? job.getCompanyName() : "N/A") + "\n" +
                            "Position : " + (job.getJobTitle() != null ? job.getJobTitle() : "N/A") + "\n" +
                            "Interview Date : " + job.getInterviewDate() + "\n" +
                            "Interview Time : " + (job.getInterviewTime() != null ? job.getInterviewTime() : "Not specified") + "\n\n" +
                            "Best of luck!\n\n" +
                            "Job Tracker Team";

            try {
                emailService.sendEmail(email, subject, body);
                sentReminderKeys.add(reminderKey);
                System.out.println("Reminder email sent to: " + email);
            } catch (Exception e) {
                System.err.println("Failed to send reminder email to " + email + ": " + e.getMessage());
            }
        }
    }
}