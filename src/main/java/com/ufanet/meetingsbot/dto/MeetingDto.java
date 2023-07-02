package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto implements Comparable<MeetingDto> {
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
                .map(AccountMeetingDto::getAccount).collect(toSet());
    }

    public Set<AccountDto> getParticipantsWithoutOwner() {
        return this.getAccountMeetings().stream()
                .map(AccountMeetingDto::getAccount)
                .filter(account -> !Objects.equals(account.getId(), this.owner.getId()))
                .collect(toSet());
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

    public Map<LocalDate, Set<ZonedDateTime>> getSortedDateMap() {
        Map<LocalDate, Set<ZonedDateTime>> dateSetMap = this.getDates().stream()
                .collect(toMap(MeetingDateDto::getDate,
                        v -> v.getMeetingTimes().stream().map(MeetingTimeDto::getDateTime)
                                .collect(toSet())));
        return new TreeMap<>(dateSetMap);
    }

    public void removeDateIf(Predicate<? super MeetingDateDto> predicate) {
        this.dates.removeIf(predicate);
    }

    @Override
    public int compareTo(MeetingDto meetingDto) {
        return this.state.compareTo(meetingDto.getState());
    }
}
