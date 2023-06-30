package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    private Long id;
    private AccountDto owner;
    private Set<AccountMeetingDto> accountMeetings;
    private String address;
    private GroupDto groupDto;
    private SubjectDto subjectDto;
    private LocalDateTime createdDt;
    private LocalDateTime updatedDt;
    private MeetingState state;
    private Set<MeetingDateDto> dates;

    public void addMeetingDate(MeetingDateDto meetingDate) {
        this.dates.add(meetingDate);
    }

    public void addParticipant(AccountDto accountDto) {
        if (this.accountMeetings == null) {
            this.accountMeetings = new HashSet<>();
        }
        AccountMeetingDto accountMeetingDto = AccountMeetingDto.builder()
                .account(accountDto).build();
        this.accountMeetings.add(accountMeetingDto);
    }


    public List<AccountTimeDto> getAccountTimes(Predicate<? super AccountTimeDto> predicate) {
        return this.dates.stream().map(MeetingDateDto::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(MeetingTimeDto::getAccountTimes).flatMap(Collection::stream)
                .filter(predicate).toList();
    }


    public Set<AccountDto> getParticipants() {
        return this.getAccountMeetings().stream()
                .map(AccountMeetingDto::getAccount).collect(Collectors.toSet());
    }

    public Set<AccountDto> getParticipantsWithoutOwner() {
        return this.getAccountMeetings().stream()
                .map(AccountMeetingDto::getAccount)
                .filter(account -> !Objects.equals(account.getId(), this.owner.getId()))
                .collect(Collectors.toSet());
    }

    public ZonedDateTime getDate() {
        return this.dates.stream().findFirst()
                .map(MeetingDateDto::getMeetingTimes)
                .get().stream().findFirst().get().getDateTime();
    }

    public ZonedDateTime getDateWithZoneId(String zoneId) {
        return this.getDate().withZoneSameInstant(ZoneId.of(zoneId));
    }

    public List<ZonedDateTime> getDatesWithZoneId(String zoneId) {
        return this.dates.stream()
                .map(MeetingDateDto::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(meetingTime -> meetingTime.getTimeWithZoneOffset(zoneId))
                .sorted().toList();
    }

    public void removeDateIf(Predicate<? super MeetingDateDto> predicate) {
        this.dates.removeIf(predicate);
    }

}
