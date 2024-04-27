package com.ufanet.meetingsbot.service.db;

import com.ufanet.meetingsbot.annotation.DatabaseTest;
import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.AccountTime;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.entity.MeetingTime;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.service.ServiceTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DatabaseTest
@ContextConfiguration(classes = ServiceTestConfiguration.class)
public class MeetingServiceDbTest {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private AccountService accountService;

    @Test
    @Sql(statements = "DELETE FROM meeting WHERE meeting.id = 1")
    void shouldReturnMeeting_whenSaveInDatabase() {
        Optional<Account> account = accountService.find(1L);
        assertTrue(account.isPresent());

        Meeting meeting = Meeting.builder()
                .owner(account.get())
                .state(MeetingState.GROUP_SELECT)
                .address("Pushkina 22")
                .build();

        Meeting saved = meetingService.save(meeting);

        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals(saved.getId(), meeting.getId());
        assertEquals(saved.getState(), meeting.getState());
        assertEquals(saved.getAddress(), meeting.getAddress());
    }

    @Test
    void shouldReturnMeeting_whenGetByMeetingId() {
        Optional<Meeting> meeting = meetingService.getByMeetingId(1L);

        assertTrue(meeting.isPresent());
        assertNotNull(meeting.get().getId());
        assertNotNull(meeting.get().getState());
        assertNotNull(meeting.get().getAddress());
    }

    @Test
    void shouldReturnMeeting_whenGetLastChangedMeetingByOwnerId() {
        Optional<Meeting> meeting = meetingService.getLastChangedMeetingByOwnerId(1);

        assertTrue(meeting.isPresent());
        assertNotNull(meeting.get().getId());
        assertNotEquals(meeting.get().getState(), MeetingState.CONFIRMED);
        assertNotEquals(meeting.get().getState(), MeetingState.PASSED);
        assertNotEquals(meeting.get().getState(), MeetingState.CANCELED);
        assertNotEquals(meeting.get().getState(), MeetingState.AWAITING);
    }

    @Test
    void shouldDeleteMeeting_whenGetByMeetingIdAndDelete() {
        long meetingId = 1L;
        Optional<Meeting> meeting = meetingService.getByMeetingId(meetingId);
        assertTrue(meeting.isPresent());

        meetingService.deleteById(meetingId);

        Optional<Meeting> deleted = meetingService.getByMeetingId(meetingId);
        assertTrue(deleted.isEmpty());
    }

    @Test
    void shouldReturnMeetingTime_whenGetByMeetingIdAndConfirmedState() {
        Optional<MeetingTime> optionalMeetingTime = meetingService.getByMeetingIdAndConfirmedState(1);
        assertTrue(optionalMeetingTime.isPresent());

        MeetingTime meetingTime = optionalMeetingTime.get();
        assertNotNull(meetingTime.getId());
        assertNotNull(meetingTime.getAccountTimes());
        assertNotNull(meetingTime.getDateTime());

        Set<AccountTime> accountTimes = meetingTime.getAccountTimes();
        assertFalse(accountTimes.isEmpty());
        assertTrue(accountTimes.stream().allMatch(t -> t.getStatus().equals(Status.CONFIRMED)));
    }
}
