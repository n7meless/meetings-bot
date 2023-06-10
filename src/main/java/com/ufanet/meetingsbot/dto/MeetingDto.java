package com.ufanet.meetingsbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long ownerId;
    private Long chatId;
    private String subjectName;
    private String address;
    private String currentDate; //editing date
    private boolean extendedTime;
    private final List<Long> userIds = new ArrayList<>();
    private final List<String> questions = new ArrayList<>();
    private final Map<String, List<String>> dateTime = new HashMap<>();
}
