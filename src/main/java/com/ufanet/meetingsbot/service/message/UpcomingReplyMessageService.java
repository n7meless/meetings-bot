package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.MeetingKeyboardMaker;
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
    private final MeetingKeyboardMaker meetingKeyboard;
    private final MessageUtils messageUtils;
    private final LocaleMessageService localeMessage;

    public void sendUpcomingMeetings(long userId, List<Meeting> meetings) {
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Meeting meeting : meetings) {
            LocalDateTime dt = meeting.getDates().stream()
                    .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                    .map(MeetingTime::getTime).findFirst().get();

            InlineKeyboardButton button =
                    meetingKeyboard.defaultInlineButton(Emojis.CLIPBOARD.getEmojiSpace() + meeting.getSubject().getTitle() + " " +
                                    Emojis.CALENDAR.getEmojiSpace() + dt.format(CustomFormatter.DATE_TIME_FORMATTER),
                            UpcomingState.UPCOMING_SELECTED_MEETING.name() + " " + meeting.getId());

            keyboard.add(List.of(button));
        }
        markup.setKeyboard(keyboard);

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessage.getMessage("upcoming.meeting.confirmed.all"), markup);

        executeMessage(editMessage);
    }

    public void sendSelectedUpcomingMeeting(long userId, Meeting meeting,
                                            List<AccountTime> accountTimes, Status selected) {

        MeetingMessage message = messageUtils.generateMeetingMessage(meeting);

        StringBuilder sb = new StringBuilder();
        sb.append(messageUtils.generateAccountLink(meeting.getOwner(),
                Emojis.CROWN.getEmojiSpace(), "")).append("\n");

        for (AccountTime accountTime : accountTimes) {
            Status status = accountTime.getStatus();
            Account account = accountTime.getAccount();

            switch (status) {
                case AWAITING -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREY_SELECTED.getEmojiSpace(), " (Опаздывает)"));
                case CONFIRMED -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREEN_SELECTED.getEmojiSpace(), " (Готов начать)"));
                case CANCELED -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREY_SELECTED.getEmojiSpace(), " (Не придет)"));
                default -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREEN_SELECTED.getEmojiSpace(), ""));

            }
            sb.append("\n");
        }
        String participants = sb.toString();

        Optional<AccountTime> accountTime = accountTimes.stream().filter(t -> t.getAccount().getId() == userId)
                .findFirst();

        String textMessage = localeMessage.getMessage("upcoming.meeting.confirmed.selected", message.subject(),
                message.questions(), message.duration(), participants,
                message.address(), meeting.getState().name());


        InlineKeyboardMarkup upcomingMarkup = meetingKeyboard.getMeetingUpcomingMarkup(userId, meeting, accountTime);

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, textMessage,
                upcomingMarkup);
        executeMessage(editMessage);


    }

    public void sendCanceledMeetingByMatching(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = messageUtils.generateAccountLink(owner, "", "");
        Set<Account> participants = meeting.getParticipants();
        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessage.getMessage("upcoming.meeting.canceled.match", ownerLink),
                    null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendReadyMeeting(Meeting meeting) {
        Set<Account> participants = meeting.getParticipants();
        Optional<LocalDateTime> readyTime = meeting.getDates().stream()
                .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                .map(MeetingTime::getTime).findFirst();
        if (readyTime.isEmpty()) return;
        LocalDateTime localDateTime = readyTime.get();
        String date = localDateTime.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER);
        String ownerLink = messageUtils.generateAccountLink(meeting.getOwner(), "", "");
        for (Account participant : participants) {
            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(participant.getId(),
                            localeMessage.getMessage("upcoming.meeting.confirmed.ready",
                                    ownerLink, Emojis.CALENDAR.getEmojiSpace() + date,
                                    Emojis.OFFICE.getEmojiSpace() + meeting.getAddress()), null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendSuccessMeetingConfirm(long userId) {
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessage.getMessage("upcoming.meeting.confirmed.message"), null);
        executeMessage(editMessage);
    }

    public void sendCanceledAccountTimeMessage(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = "<a href='https://t.me/" + owner.getUsername() + "'>" + owner.getFirstname() + "</a>";
        Set<Account> participants = meeting.getParticipants();
        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessage.getMessage("upcoming.meeting.canceled.participants",
                            ownerLink), null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendEditMeetingAccountTimes(long userId, Meeting meeting, List<AccountTime> accountTimes) {

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);
        String textMeeting = localeMessage.getMessage("create.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());


        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getChangeMeetingTimeKeyboard(meeting.getId(), accountTimes);
        EditMessageText sendMessage = messageUtils.generateEditMessageHtml(userId, textMeeting, keyboard);

        executeMessage(sendMessage);
    }

    public void sendMeetingsNotExist(long userId) {
        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessage.getMessage("upcoming.meeting.notexist"), null);
        executeMessage(editMessage);
    }

    public void sendCanceledMeetingByOwner(long userId, Meeting meeting) {
        LocalDateTime date = meeting.getMeetingDate();
        String accountLink = messageUtils.generateAccountLink(meeting.getOwner(), "", "");
        String messageToParticipants = localeMessageService.getMessage("upcoming.meeting.canceled.owner",
                Emojis.CALENDAR.getEmojiSpace() + date.format(CustomFormatter.DATE_TIME_FORMATTER), accountLink);

        Set<Account> participants = meeting.getParticipantsWithoutOwner();
        for (Account account : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(account.getId(),
                    messageToParticipants, null);
            executeSendMessage(sendMessage);
        }
        SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId,
                localeMessage.getMessage("upcoming.meeting.canceled.success"), null);
        executeSendMessage(sendMessage);
    }
}
