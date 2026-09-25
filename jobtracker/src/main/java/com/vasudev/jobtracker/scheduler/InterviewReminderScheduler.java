package com.vasudev.jobtracker.scheduler;

import com.vasudev.jobtracker.service.InterviewReminderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class InterviewReminderScheduler {

    private final InterviewReminderService interviewReminderService;

    public InterviewReminderScheduler(InterviewReminderService interviewReminderService) {
        this.interviewReminderService = interviewReminderService;
    }

    // ========================================
    // Runs daily at 9:00 AM
    // ========================================
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendInterviewReminder() {

        System.out.println("--------------------------------");
        System.out.println("Running Scheduled Interview Reminders...");
        System.out.println("Today's Date : " + LocalDate.now());
        System.out.println("--------------------------------");

        interviewReminderService.sendTodayInterviewReminders();
    }
}
