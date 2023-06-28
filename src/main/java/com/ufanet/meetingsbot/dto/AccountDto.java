package com.ufanet.meetingsbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private String language;
    private String timeZone;
    private long groupId;
    private Set<LocalDateTime> meetingDateTimes = new TreeSet<>();
}
