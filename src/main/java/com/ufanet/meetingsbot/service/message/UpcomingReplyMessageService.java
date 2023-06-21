package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.UpcomingState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
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
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final MessageUtils messageUtils;
    private final LocaleMessageService localeMessageService;

    public void sendUpcomingMeetings(long userId) {
        List<Meeting> meetings = meetingService.getMeetingsByUserIdAndState(userId, MeetingState.CONFIRMED);

//        MeetingMessage message = messageUtils.generateMeetingMessage(meeting);

//        String textMessage = localeMessageService.getMessage("reply.meeting.upcoming.all", message.subject(),
//                message.questions(), message.participants(), message.duration(),
//                message.address(), meeting.getState().name());

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Meeting meeting : meetings) {
            LocalDateTime dt = meeting.getDates().stream()
                    .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                    .map(MeetingTime::getTime).findFirst().get();

            InlineKeyboardButton k1 = InlineKeyboardButton.builder()
                    .text(Emojis.CALENDAR.getEmojiSpace() + dt.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER))
                    .callbackData(UpcomingState.UPCOMING_MEETINGS.name() + " " + meeting.getId()).build();
            keyboard.add(List.of(k1));
        }
        markup.setKeyboard(keyboard);

        SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId,
                "Выберите предстоящие встречи", markup);

        executeSendMessage(sendMessage);
    }

    public void sendUpcomingMeetingsByMeetingId(long userId, long meetingId) {
        Optional<Meeting> optionalMeeting =
                meetingService.getById(meetingId);
        Meeting meeting = optionalMeeting.get();
        MeetingMessage message = messageUtils.generateMeetingMessage(meeting);
        String textMessage = localeMessageService.getMessage("reply.meeting.upcoming.all", message.subject(),
                message.questions(), message.duration(), message.participants(),
                message.address(), meeting.getState().name());

        SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId, textMessage, null);

        executeSendMessage(sendMessage);
    }

    public void sendCanceledMeetingByMatching(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = "<a href='https://t.me/" + owner.getUsername() + "'>" + owner.getFirstname() + "</a>";
        Set<Account> participants = meeting.getParticipants();
        participants.add(owner);
        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessageService.getMessage("reply.meeting.canceled.match", ownerLink),
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
        //TODO localemessage
        String text = "Время запланировано на " + localDateTime.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER);

        for (Account participant : participants) {
            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(participant.getId(), text, null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendSuccessMeetingConfirm(long userId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessageService.getMessage("reply.meeting.confirmed.message"));
        executeSendMessage(sendMessage);
    }

    public void sendCanceledAccountTimeMessage(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = "<a href='https://t.me/" + owner.getUsername() + "'>" + owner.getFirstname() + "</a>";
        Set<Account> participants = meeting.getParticipants();
        participants.add(owner);
        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessageService.getMessage("reply.meeting.canceled.participants",
                            ownerLink), null);

            executeSendMessage(sendMessage);
        }
        participants.remove(owner);
    }

    public void editMeetingToParticipant(long userId, Meeting meeting, List<AccountTime> accountTimes) {

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);
        String textMeeting = localeMessageService.getMessage("reply.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());


        InlineKeyboardMarkup keyboard =
                meetingInlineKeyboardMaker.getChangeMeetingTimeKeyboard(meeting.getId(), accountTimes);
        EditMessageText sendMessage = messageUtils.generateEditMessageHtml(userId, textMeeting, keyboard);

        executeEditMessage(sendMessage);
    }

}
