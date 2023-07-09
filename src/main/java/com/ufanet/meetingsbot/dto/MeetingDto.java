package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long id;
    private AccountDto owner;
    private Set<AccountDto> participants;
    private String address;
    private GroupDto groupDto;
    private SubjectDto subjectDto;
    private LocalDateTime createdDt;
    private LocalDateTime updatedDt;
    private MeetingState state;
    private Set<MeetingDateDto> dates;

    public MeetingDto(AccountDto owner) {
        this.owner = owner;
        this.createdDt = LocalDateTime.now();
        this.updatedDt = LocalDateTime.now();
        this.dates = new HashSet<>();
        this.participants = Set.of(owner);
        this.state = MeetingState.GROUP_SELECT;
    }

    public List<AccountTimeDto> getAccountTimes(Predicate<? super AccountTimeDto> predicate) {
        return this.dates.stream().map(MeetingDateDto::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(MeetingTimeDto::getAccountTimes).flatMap(Collection::stream)
                .filter(predicate).toList();
    }

    public Set<AccountDto> getParticipantsWithoutOwner() {
        return this.participants.stream()
                .filter(account -> !Objects.equals(account.getId(), this.owner.getId()))
                .collect(toSet());
    }

    public ZonedDateTime getDate() {
        return this.dates.stream().findFirst()
                .map(MeetingDateDto::getMeetingTimes)
                .get().stream().findFirst().get().getDateTime();
    }

    public List<ZonedDateTime> getDatesWithZoneId(String zoneId) {
        return this.dates.stream()
                .map(MeetingDateDto::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(meetingTime -> meetingTime.getTimeWithZoneOffset(zoneId))
                .sorted().toList();
    }

    public void removeDatesIf(Predicate<? super MeetingDateDto> predicate) {
        this.dates.removeIf(predicate);
    }
}
