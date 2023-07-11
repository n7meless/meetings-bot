package com.ufanet.meetingsbot.service.mock;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.repository.MeetingRepository;
import com.ufanet.meetingsbot.service.MeetingService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceMockTest {
    @InjectMocks
    private MeetingService meetingService;
    @Mock
    private MeetingRepository meetingRepository;

    private Meeting dummyMeeting(long meetingId) {
        return Meeting.builder().address("address").id(meetingId)
                .createdDt(LocalDateTime.now()).state(MeetingState.GROUP_SELECT)
                .updatedDt(LocalDateTime.now())
                .build();
    }

    @Test
    public void shouldReturnMeetingWhenSave() {
        //given
        Meeting dummyMeeting = dummyMeeting(1L);

        //when + then
        Mockito.when(meetingRepository.save(Mockito.any(Meeting.class))).thenReturn(dummyMeeting);
        Meeting meeting = meetingService.save(dummyMeeting);

        Assertions.assertNotNull(meeting);
        Assertions.assertSame(meeting, dummyMeeting);
        Assertions.assertEquals(meeting.getId(), dummyMeeting.getId());
        Assertions.assertEquals(meeting.getState(), dummyMeeting.getState());
        Assertions.assertEquals(meeting.getCreatedDt(), dummyMeeting.getCreatedDt());
        Assertions.assertEquals(meeting.getUpdatedDt(), dummyMeeting.getUpdatedDt());
        Assertions.assertEquals(meeting.getAddress(), dummyMeeting.getAddress());
        Assertions.assertEquals(meeting.getState(), dummyMeeting.getState());

    }

    @Test
    public void shouldReturnLastChangedMeetingWhereStatesNotIn() {
        //given
        Meeting dummyMeeting = dummyMeeting(1L);

        //when + then
        Mockito.when(meetingRepository.findByOwnerIdAndStateIsNotIn(1L,
                        List.of(MeetingState.CONFIRMED, MeetingState.PASSED, MeetingState.AWAITING, MeetingState.CANCELED)))
                .thenReturn(Optional.ofNullable(dummyMeeting));
        Optional<Meeting> meeting = meetingService.getLastChangedMeetingByOwnerId(1L);

        Assertions.assertTrue(meeting.isPresent());
        Assertions.assertEquals(meeting.get().getId(), dummyMeeting.getId());
        Assertions.assertEquals(meeting.get().getState(), dummyMeeting.getState());
        Assertions.assertEquals(meeting.get().getCreatedDt(), dummyMeeting.getCreatedDt());
        Assertions.assertEquals(meeting.get().getUpdatedDt(), dummyMeeting.getUpdatedDt());
        Assertions.assertEquals(meeting.get().getAddress(), dummyMeeting.getAddress());
        Assertions.assertEquals(meeting.get().getState(), dummyMeeting.getState());
    }

    @Test
    public void shouldReturnMeetingWhenGetById() {
        //given
        Meeting dummyMeeting = dummyMeeting(1L);

        //when + then
        Mockito.when(meetingRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(dummyMeeting));
        Optional<Meeting> meeting = meetingService.getByMeetingId(Mockito.anyLong());

        Assertions.assertTrue(meeting.isPresent());
        Assertions.assertEquals(meeting.get().getId(), dummyMeeting.getId());
        Assertions.assertEquals(meeting.get().getState(), dummyMeeting.getState());
        Assertions.assertEquals(meeting.get().getCreatedDt(), dummyMeeting.getCreatedDt());
        Assertions.assertEquals(meeting.get().getUpdatedDt(), dummyMeeting.getUpdatedDt());
        Assertions.assertEquals(meeting.get().getAddress(), dummyMeeting.getAddress());
        Assertions.assertEquals(meeting.get().getState(), dummyMeeting.getState());
    }

    @Test
    public void shouldReturnMeetingsWhenGetByUserIdAndStateIn() {
        //given
        List<Meeting> dummyMeetings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meeting dummyMeeting = dummyMeeting(i);
            dummyMeetings.add(dummyMeeting);
        }

        //when + then
        Mockito.when(meetingRepository.findMeetingsByUserIdAndStateIn(1L, List.of(MeetingState.AWAITING)))
                .thenReturn(dummyMeetings);
        List<Meeting> meetings = meetingService.getMeetingsByUserIdAndStateIn(1L, List.of(MeetingState.AWAITING));

        Assertions.assertFalse(meetings.isEmpty());
        Assertions.assertEquals(meetings.size(), dummyMeetings.size());
        Assertions.assertSame(meetings, dummyMeetings);
    }

    @Test
    public void shouldReturnMeetingsWhenGetConfirmedMeetingsWhereDatesMinutesBetween() {
        //given
        List<Meeting> dummyMeetings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meeting dummyMeeting = dummyMeeting(i);
            dummyMeetings.add(dummyMeeting);
        }
        ZonedDateTime now = ZonedDateTime.now();

        //when + then
        Mockito.when(meetingRepository.findConfirmedMeetingsWhereDateMinutesBetween(now, 1))
                .thenReturn(dummyMeetings);
        List<Meeting> meetings = meetingService.getConfirmedMeetingsWhereDateMinutesBetween(now, 1);

        Assertions.assertFalse(meetings.isEmpty());
        Assertions.assertEquals(meetings.size(), dummyMeetings.size());
        Assertions.assertSame(meetings, dummyMeetings);
    }

    @Test
    public void shouldSaveAllMeetings() {
        //given
        List<Meeting> dummyMeetings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meeting dummyMeeting = dummyMeeting(i);
            dummyMeetings.add(dummyMeeting);
        }

        //when + then
        Mockito.when(meetingRepository.saveAll(Mockito.anyList()))
                .thenReturn(dummyMeetings);
        List<Meeting> meetings = meetingService.saveAll(dummyMeetings);

        Assertions.assertFalse(meetings.isEmpty());
        Assertions.assertEquals(meetings.size(), dummyMeetings.size());
        Assertions.assertSame(meetings, dummyMeetings);
    }

    @Test
    public void shouldReturnMeetingsWhenGetConfirmedMeetingsWhereDatesLaterThanSubjectDuration() {
        //given
        List<Meeting> dummyMeetings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meeting dummyMeeting = dummyMeeting(i);
            dummyMeetings.add(dummyMeeting);
        }
        ZonedDateTime now = ZonedDateTime.now();

        //when + then
        Mockito.when(meetingRepository.findConfirmedMeetingsWhereDatesLaterThanSubjectDuration(now))
                .thenReturn(dummyMeetings);
        List<Meeting> meetings = meetingService.getConfirmedMeetingsWhereDatesLaterThanSubjectDuration(now);

        Assertions.assertFalse(meetings.isEmpty());
        Assertions.assertEquals(meetings.size(), dummyMeetings.size());
        Assertions.assertSame(meetings, dummyMeetings);
    }
}
