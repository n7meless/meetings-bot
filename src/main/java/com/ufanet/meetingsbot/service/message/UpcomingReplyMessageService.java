package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.MeetingKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import com.ufanet.meetingsbot.utils.Emojis;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UpcomingReplyMessageService extends ReplyMessageService {
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final MeetingKeyboardMaker meetingKeyboard;

    public void sendUpcomingMeetings(long userId, List<Meeting> meetings) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        Map<MeetingState, List<Meeting>> meetingMap =
                meetings.stream().sorted(Meeting::compareTo).collect(Collectors.groupingBy(Meeting::getState));

        for (Map.Entry<MeetingState, List<Meeting>> entry : meetingMap.entrySet()) {
            MeetingState entryState = entry.getKey();
            List<Meeting> entryValue = entry.getValue();
            if (entryValue.size() > 0) {
                if (entryState.equals(MeetingState.AWAITING)) {
                    InlineKeyboardButton button = meetingKeyboard.defaultInlineButton(
                            Emojis.ORANGE_CIRCLE.getEmojiSpace() + "В ожидании", " ");
                    keyboard.add(List.of(button));
                } else {
                    InlineKeyboardButton button = meetingKeyboard.defaultInlineButton(
                            Emojis.GREEN_CIRCLE.getEmojiSpace() + "Подтвержденные", " ");
                    keyboard.add(List.of(button));
                }
                int i = 0;
                while (i < entryValue.size()) {
                    List<InlineKeyboardButton> rowButtons = new ArrayList<>();
                    for (int j = 0; j < 2 && i < entryValue.size(); j++, i++) {
                        Meeting meeting = entryValue.get(i);
                        ZonedDateTime zonedDateTime = meeting.getDate().withZoneSameInstant(ZoneId.of(zoneId));

                        String upcomingMeeting = messageUtils.buildText(Emojis.CALENDAR.getEmojiSpace(),
                                zonedDateTime.format(CustomFormatter.DATE_TIME_FORMATTER));

                        String callback = messageUtils.buildText(UpcomingState.UPCOMING_SELECTED_MEETING.name(),
                                " ", Long.toString(meeting.getId()));

                        InlineKeyboardButton button =
                                meetingKeyboard.defaultInlineButton(upcomingMeeting, callback);

                        rowButtons.add(button);
                    }
                    keyboard.add(rowButtons);
                }
            }
        }

        InlineKeyboardMarkup markup = meetingKeyboard.buildInlineMarkup(keyboard);
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("upcoming.meeting.confirmed.all"), markup);

        executeMessage(editMessage);
    }

    public void sendSelectedUpcomingMeeting(long userId, Meeting meeting,
                                            List<AccountTime> accountTimes) {
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

        String textMessage = localeMessageService.getMessage("upcoming.meeting.confirmed.selected",
                message.subject(), message.questions(), message.duration(),
                participants, message.address(), meeting.getState().name());

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
                    localeMessageService.getMessage("upcoming.meeting.canceled.match", ownerLink),
                    null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendReadyMeeting(Meeting meeting) {
        Set<Account> participants = meeting.getParticipants();
        Optional<ZonedDateTime> readyTime = meeting.getDates().stream()
                .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                .map(MeetingTime::getDateTime).findFirst();

        if (readyTime.isEmpty()) return;
        String ownerLink = messageUtils.generateAccountLink(meeting.getOwner(), "", "");

        for (Account participant : participants) {
            String zoneId = participant.getZoneId();
            ZonedDateTime zonedDateTime = readyTime.get();
            String date = zonedDateTime.withZoneSameInstant(ZoneId.of(zoneId))
                    .format(CustomFormatter.DATE_TIME_WEEK_FORMATTER);

            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(participant.getId(),
                            localeMessageService.getMessage("upcoming.meeting.confirmed.ready",
                                    ownerLink, Emojis.CALENDAR.getEmojiSpace() + date,
                                    Emojis.OFFICE.getEmojiSpace() + meeting.getAddress()), null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendSuccessMeetingConfirm(long userId) {
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("upcoming.meeting.confirmed.message"), null);
        executeMessage(editMessage);
    }

    public void sendCanceledAccountTimeMessage(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = messageUtils.generateAccountLink(owner, "", "");
        Set<Account> participants = meeting.getParticipants();
        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessageService.getMessage("upcoming.meeting.canceled.participants",
                            ownerLink), null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendEditMeetingAccountTimes(long userId, Meeting meeting, List<AccountTime> accountTimes) {
        Account account = accountService.getByUserId(userId).orElseThrow();
        String zoneId = account.getZoneId();
        List<ZonedDateTime> dates = meeting.getDatesWithZoneId(zoneId);
        String timesText = messageUtils.generateDatesText(dates);

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);
        String textMeeting = localeMessageService.getMessage("create.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), timesText,
                meetingMessage.address());

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getChangeMeetingTimeKeyboard(meeting.getId(), accountTimes, zoneId);
        EditMessageText sendMessage = messageUtils.generateEditMessageHtml(userId, textMeeting, keyboard);

        executeMessage(sendMessage);
    }

    public void sendSelectedAwaitingMeeting(long userId, Meeting meeting) {
        Account account = accountService.getByUserId(userId).orElseThrow();

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getMeetingConfirmKeyboard(meeting);

        String zoneId = account.getZoneId();
        List<ZonedDateTime> dates = meeting.getDatesWithZoneId(zoneId);
        String datesText = messageUtils.generateDatesText(dates);

        String textMeeting = localeMessageService.getMessage("create.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), datesText,
                meetingMessage.address());

        SendMessage sendMessage =
                messageUtils.generateSendMessageHtml(account.getId(), textMeeting, keyboard);

        executeSendMessage(sendMessage);
    }


    public void sendMeetingsNotExist(long userId) {
        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessageService.getMessage("upcoming.meeting.notexist"), null);
        executeMessage(editMessage);
    }

    public void sendCanceledMeetingByOwner(long userId, Meeting meeting) {
        ZonedDateTime zonedDateTime = meeting.getDate();
        String accountLink = messageUtils.generateAccountLink(meeting.getOwner(), "", "");

        List<Account> accounts = accountService.getAccountByMeetingId(meeting.getId());
        for (Account account : accounts) {
            if (Objects.equals(account.getId(), meeting.getOwner().getId())) continue;

            String zoneId = account.getZoneId();
            String dateWithZone = zonedDateTime.withZoneSameInstant(ZoneId.of(zoneId))
                    .format(CustomFormatter.DATE_TIME_FORMATTER);

            String messageToParticipants = localeMessageService.getMessage("upcoming.meeting.canceled.owner",
                    Emojis.CALENDAR.getEmojiSpace() + dateWithZone,
                    accountLink);

            SendMessage sendMessage = messageUtils.generateSendMessageHtml(account.getId(),
                    messageToParticipants, null);
            executeSendMessage(sendMessage);
        }
        SendMessage sendMessage = messageUtils.generateSendMessageHtml(userId,
                localeMessageService.getMessage("upcoming.meeting.canceled.success"), null);
        executeSendMessage(sendMessage);
    }

    public void sendSelectedReadyMeeting(long userId, Meeting meeting) {
        InlineKeyboardButton btn1 = meetingKeyboard.defaultInlineButton("Назад",
                UpcomingState.UPCOMING_MEETINGS.name());
        InlineKeyboardButton btn2 = meetingKeyboard.defaultInlineButton("Отменить встречу",
                UpcomingState.UPCOMING_CANCEL_BY_OWNER.name() + " " + meeting.getId());

        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessageService.getMessage("upcoming.meeting.selected.owner"),
                meetingKeyboard.buildInlineMarkup(List.of(List.of(btn1, btn2))));
        executeSendMessage(sendMessage);
    }

    public void sendConfirmedComingMeeting(Meeting meeting) {
        Set<Account> accounts = meeting.getParticipants();
        ZonedDateTime zonedDateTime = meeting.getDate();
        for (Account account : accounts) {
            String zoneId = account.getZoneId();
            ZonedDateTime accountZoneTime = zonedDateTime.withZoneSameInstant(ZoneId.of(zoneId));
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(account.getId(),
                    localeMessageService.getMessage("upcoming.meeting.confirmed.coming",
                            Emojis.ALARM_CLOCK.getEmojiSpace() + accountZoneTime.toLocalTime()), null);
            executeSendMessage(sendMessage);
        }
    }

    public void sendPassedMeetings(long userId) {
        List<Meeting> passedMeetings =
                meetingService.getMeetingsByUserIdAndStateIn(userId, List.of(MeetingState.PASSED));

    }

    public void sendCommentNotificationParticipants(Meeting meeting) {
        Set<Account> participants = meeting.getParticipants();
        String ownerLink = messageUtils.generateAccountLink(meeting.getOwner(), "", "");
        String text = localeMessageService.getMessage("upcoming.meeting.notification.comment", ownerLink);

        InlineKeyboardButton skip = meetingKeyboard.defaultInlineButton("Пропустить", " ");

        for (Account participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(), text,
                    meetingKeyboard.buildInlineMarkup(List.of(List.of(skip))));
            executeSendMessage(sendMessage);
        }
    }
}
