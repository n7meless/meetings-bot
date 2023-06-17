package com.ufanet.meetingsbot.dto;

public record MeetingMessage(String owner, String participants, String subject,
                             String questions, String duration, String times, String address) {
}
