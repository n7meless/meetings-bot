package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.*;
import com.ufanet.meetingsbot.mapper.AccountMapper;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.MeetingDate;
import com.ufanet.meetingsbot.model.MeetingTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ufanet.meetingsbot.constants.ToggleButton.NEXT;
import static com.ufanet.meetingsbot.constants.ToggleButton.PREV;

@Service
@RequiredArgsConstructor
public class MeetingConstructor {
    private final AccountMapper accountMapper;

    public MeetingDto create(Account account) {
        AccountDto accountDto = accountMapper.map(account);
        MeetingDto meetingDto = MeetingDto.builder().owner(accountDto)
                .createdDt(LocalDateTime.now())
                .updatedDt(LocalDateTime.now())
                .dates(new HashSet<>())
                .state(MeetingState.GROUP_SELECT).build();

        meetingDto.addParticipant(accountDto);
        return meetingDto;
    }

    public void updateParticipants(MeetingDto meetingDto, long participantId, Set<Account> accounts) {
        Set<AccountMeetingDto> participantIds = meetingDto.getAccountMeetings();
        boolean removed = participantIds.removeIf(t -> t.getAccount().getId() == participantId);
        if (!removed) {
            AccountDto accountDto = accounts.stream()
                    .filter(account -> account.getId() == participantId)
                    .map(accountMapper::map).findFirst().orElseThrow();
            AccountMeetingDto accountMeetingDto = AccountMeetingDto.builder()
                    .account(accountDto).build();
            participantIds.add(accountMeetingDto);
        }
        meetingDto.setAccountMeetings(participantIds);
    }

    public void updateQuestion(MeetingDto meetingDto, String question) {
        SubjectDto subjectDto = meetingDto.getSubjectDto();
        Set<String> questions = subjectDto.getQuestions();
        if (questions.contains(question)) {
            questions.remove(question);
        } else questions.add(question);
        subjectDto.setQuestions(questions);
        meetingDto.setSubjectDto(subjectDto);
    }

    public void updateDate(MeetingDto meetingDto, String callback) {
        Set<MeetingDateDto> datesMap = meetingDto.getDates();
        if (!callback.startsWith(NEXT.name()) && !callback.startsWith(PREV.name())) {
            LocalDate localDate = LocalDate.parse(callback);
            Optional<MeetingDateDto> meetingDateDto =
                    datesMap.stream().filter(dto -> dto.getDate().equals(localDate)).findFirst();
            if (meetingDateDto.isPresent()) {
                datesMap.remove(meetingDateDto.get());
            } else {
                MeetingDateDto build = MeetingDateDto.builder()
                        .date(localDate).build();
                datesMap.add(build);
            }
        }
        meetingDto.setDates(datesMap);
    }

    public void updateTime(MeetingDto meetingDto, String callback) {
        ZonedDateTime znd = ZonedDateTime.parse(callback);
        LocalDate localDate = znd.toLocalDate();
        Set<MeetingDateDto> datesMap = meetingDto.getDates();
        Optional<MeetingDateDto> meetingDateDto = datesMap.stream()
                .filter(dto -> dto.getDate().isEqual(localDate)).findFirst();

        if (meetingDateDto.isPresent()) {
            MeetingDateDto dateDto = meetingDateDto.get();
            Set<MeetingTimeDto> meetingTimes = dateDto.getMeetingTimes();
            boolean removed = meetingTimes.removeIf(mtd -> mtd.getDateTime().isEqual(znd));
            if (!removed) {
                MeetingTimeDto meetingTimeDto = MeetingTimeDto.builder().dateTime(znd).build();
                meetingTimes.add(meetingTimeDto);
            }
            dateDto.setMeetingTimes(meetingTimes);
        }
    }

    public void updateAccountTimes(MeetingDto meetingDto, MeetingTime meetingTime) {
        Set<MeetingDateDto> dates = meetingDto.getDates();
        MeetingDate meetingDate = meetingTime.getMeetingDate();

        dates.removeIf(dto -> !Objects.equals(dto.getId(), meetingDate.getId()));
        MeetingDateDto dateDto = dates.stream().findFirst().orElseThrow();

        Set<MeetingTimeDto> meetingTimes = dateDto.getMeetingTimes();
        meetingTimes.removeIf(dto -> !Objects.equals(dto.getId(), meetingTime.getId()));
        MeetingTimeDto meetingTimeDto = meetingTimes.stream().findFirst().orElseThrow();

        Set<AccountTimeDto> accountTimeDtos = meetingTime.getAccountTimes().stream()
                .map(at -> AccountTimeDto.builder().status(at.getStatus())
                        .account(accountMapper.map(at.getAccount()))
                        .id(at.getId()).meetingTime(meetingTimeDto).build())
                .collect(Collectors.toSet());
        meetingTimeDto.setAccountTimes(accountTimeDtos);
    }
}
