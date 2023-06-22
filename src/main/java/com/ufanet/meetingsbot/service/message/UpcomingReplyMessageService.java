package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.LocaleMessageService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import com.ufanet.meetingsbot.utils.Emojis;
import com.ufanet.meetingsbot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UpcomingReplyMessageService extends ReplyMessageService {
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final MeetingInlineKeyboardMaker meetingKeyboard;
    private final MessageUtils messageUtils;
    private final LocaleMessageService localeMessage;

    public void sendSelectedUpcomingMeeting(long userId, List<Meeting> meetings) {
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Meeting meeting : meetings) {
            LocalDateTime dt = meeting.getDates().stream()
                    .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                    .map(MeetingTime::getTime).findFirst().get();

            InlineKeyboardButton button =
                    meetingKeyboard.defaultInlineButton(Emojis.CALENDAR.getEmojiSpace() + dt.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER),
                            UpcomingState.UPCOMING_MEETINGS.name() + " " + meeting.getId());

            keyboard.add(List.of(button));
        }
        markup.setKeyboard(keyboard);

        SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId,
                localeMessage.getMessage("reply.meeting.upcoming.all"), markup);

        executeSendMessage(sendMessage);
    }

    public void sendSelectedUpcomingMeeting(long userId, long meetingId) {
        Optional<Meeting> optionalMeeting =
                meetingService.getById(meetingId);
        Meeting meeting = optionalMeeting.get();
        MeetingMessage message = messageUtils.generateMeetingMessage(meeting);
        String textMessage = localeMessage.getMessage("reply.meeting.upcoming.send", message.subject(),
                message.questions(), message.duration(), message.participants(),
                message.address(), meeting.getState().name());

        SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId, textMessage, null);

        executeSendMessage(sendMessage);
    }

    public void sendCanceledMeetingByMatching(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = messageUtils.generateAccountLink(owner, "");
        Set<Account> participants = meeting.getParticipants();
        participants.add(owner);
        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessage.getMessage("reply.meeting.canceled.match", ownerLink),
                    null);

            executeSendMessage(sendMessage);
        }
        participants.remove(owner);
    }

    public void sendReadyMeeting(Meeting meeting) {
        Set<Account> participants = meeting.getParticipants();
        Optional<LocalDateTime> readyTime = meeting.getDates().stream()
                .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                .map(MeetingTime::getTime).findFirst();
        if (readyTime.isEmpty()) return;
        LocalDateTime localDateTime = readyTime.get();
        String date = localDateTime.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER);
        String ownerLink = messageUtils.generateAccountLink(meeting.getOwner(), "");
        for (Account participant : participants) {
            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(participant.getId(),
                            localeMessage.getMessage("reply.meeting.ready",
                                    ownerLink, Emojis.CALENDAR.getEmojiSpace() + date,
                                    Emojis.OFFICE.getEmojiSpace() + meeting.getAddress()), null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendSuccessMeetingConfirm(long userId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessage.getMessage("reply.meeting.confirmed.message"));
        executeSendMessage(sendMessage);
    }

    public void sendCanceledAccountTimeMessage(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = "<a href='https://t.me/" + owner.getUsername() + "'>" + owner.getFirstname() + "</a>";
        Set<Account> participants = meeting.getParticipants();
        participants.add(owner);
        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessage.getMessage("reply.meeting.canceled.participants",
                            ownerLink), null);

            executeSendMessage(sendMessage);
        }
        participants.remove(owner);
    }

    public void editMeetingToParticipant(long userId, Meeting meeting, List<AccountTime> accountTimes) {

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);
        String textMeeting = localeMessage.getMessage("reply.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());


        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getChangeMeetingTimeKeyboard(meeting.getId(), accountTimes);
        EditMessageText sendMessage = messageUtils.generateEditMessageHtml(userId, textMeeting, keyboard);

        executeEditMessage(sendMessage);
    }

    public void sendMeetingsNotExist(long userId) {
        SendMessage sendMessage =
                messageUtils.generateSendMessage(userId,
                        localeMessage.getMessage("reply.meeting.upcoming.notexist"), null);
        executeSendMessage(sendMessage);
    }
}
