package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long id;
    private AccountDto owner;
    private Set<AccountDto> participants;
    private String address;
    private long groupId;
    private String subjectTitle;
    private int subjectDuration = 0;
    private Set<String> questions;
    private LocalDateTime createdDt;
    private LocalDateTime updatedDt;
    private MeetingState state;
    private Map<LocalDate, Set<ZonedDateTime>> datesMap;

    public MeetingDto(long ownerId) {
        this.owner = AccountDto.builder().id(ownerId).build();
        this.createdDt = LocalDateTime.now();
        this.updatedDt = LocalDateTime.now();
        this.datesMap = new TreeMap<>();
        this.subjectDuration = 0;
        this.questions = new HashSet<>();
        this.participants = new HashSet<>();
        this.state = MeetingState.GROUP_SELECT;
    }


    public List<ZonedDateTime> getDatesWithZoneId(String zoneId) {
        return this.datesMap.values().stream().flatMap(Collection::stream)
                .map(t -> t.withZoneSameInstant(ZoneId.of(zoneId))).toList();
    }

    public ZonedDateTime getDate() {
        return this.datesMap.values().stream()
                .flatMap(Collection::stream)
                .findFirst().orElseGet(ZonedDateTime::now);
    }
}
