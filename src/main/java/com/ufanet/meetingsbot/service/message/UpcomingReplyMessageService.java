package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.exceptions.AccountNotFoundException;
import com.ufanet.meetingsbot.keyboard.MeetingKeyboardMaker;
import com.ufanet.meetingsbot.mapper.AccountMapper;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.AccountMeeting;
import com.ufanet.meetingsbot.model.AccountTime;
import com.ufanet.meetingsbot.model.Meeting;
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
    private final AccountMapper accountMapper;

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

    public void sendSelectedUpcomingMeeting(long userId, MeetingDto meetingDto,
                                            List<AccountTimeDto> accountTimes) {
        MeetingMessage message = messageUtils.generateMeetingMessage(meetingDto);

        StringBuilder sb = new StringBuilder();
        sb.append(messageUtils.generateAccountLink(meetingDto.getOwner(),
                Emojis.CROWN.getEmojiSpace(), "")).append("\n");

        for (AccountTimeDto accountTime : accountTimes) {
            Status status = accountTime.getStatus();
            AccountDto account = accountTime.getAccount();
            switch (status) {
                case AWAITING -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREY_SELECTED.getEmojiSpace(), " (Опаздывает)"));
                case READY -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREEN_SELECTED.getEmojiSpace(), " (Готов начать)"));
                case CANCELED -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREY_SELECTED.getEmojiSpace(), " (Не придет)"));
                case CONFIRMED -> sb.append(messageUtils.generateAccountLink(account,
                        Emojis.GREEN_SELECTED.getEmojiSpace(), ""));
            }
            sb.append("\n");
        }
        String participants = sb.toString();

        Optional<AccountTimeDto> accountTime = accountTimes.stream().filter(t -> t.getAccount().getId() == userId)
                .findFirst();

        String textMessage = localeMessageService.getMessage("upcoming.meeting.confirmed.selected",
                message.subject(), message.questions(), message.duration(),
                participants, message.address(), meetingDto.getState().name());

        InlineKeyboardMarkup upcomingMarkup =
                meetingKeyboard.getMeetingUpcomingMarkup(userId, meetingDto, accountTime);

        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, textMessage,
                upcomingMarkup);
        executeMessage(editMessage);
    }

    public void sendCanceledMeetingByMatching(MeetingDto meetingDto) {
        AccountDto owner = meetingDto.getOwner();
        String ownerLink = messageUtils.generateAccountLink(owner, "", "");
        Set<AccountDto> participants = meetingDto.getParticipants();
        for (AccountDto participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessageService.getMessage("upcoming.meeting.canceled.match", ownerLink),
                    null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendSuccessMeetingConfirm(long userId) {
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("upcoming.meeting.confirmed.message"), null);
        executeMessage(editMessage);
    }

    public void sendCanceledAccountTimeMessage(MeetingDto meetingDto) {
        AccountDto owner = meetingDto.getOwner();
        String ownerLink = messageUtils.generateAccountLink(owner, "", "");
        Set<AccountDto> participants = meetingDto.getParticipants();
        for (AccountDto participant : participants) {
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(),
                    localeMessageService.getMessage("upcoming.meeting.canceled.participants",
                            ownerLink), null);

            executeSendMessage(sendMessage);
        }
    }

    public void sendEditMeetingAccountTimes(long userId, MeetingDto meetingDto,
                                            List<AccountTime> accountTimes) {
        Account account = accountService.getByUserId(userId)
                .orElseThrow(()->new AccountNotFoundException(userId));
        String zoneId = account.getZoneId();
        List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
        String timesText = messageUtils.generateDatesText(dates);

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);
        String textMeeting = localeMessageService.getMessage("create.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), timesText,
                meetingMessage.address());

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getChangeMeetingTimeKeyboard(meetingDto.getId(), accountTimes, zoneId);
        EditMessageText sendMessage = messageUtils.generateEditMessageHtml(userId, textMeeting, keyboard);

        executeMessage(sendMessage);
    }

    public void sendSelectedAwaitingMeeting(long userId, MeetingDto meetingDto) {
        Account account = accountService.getByUserId(userId)
                .orElseThrow(()->new AccountNotFoundException(userId));

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getMeetingConfirmKeyboard(meetingDto.getId());

        String zoneId = account.getZoneId();
        List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
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

    public void sendCanceledMeetingByOwner(long userId, MeetingDto meetingDto) {
        ZonedDateTime zonedDateTime = meetingDto.getDate();
        String accountLink = messageUtils.generateAccountLink(meetingDto.getOwner(), "", "");

        List<Account> accounts = accountService.getAccountsByMeetingId(meetingDto.getId());
        for (Account account : accounts) {
            if (Objects.equals(account.getId(), meetingDto.getOwner().getId())) continue;

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

    public void sendSelectedReadyMeeting(long userId, MeetingDto meetingDto) {
        InlineKeyboardButton btn1 = meetingKeyboard.defaultInlineButton("Назад",
                UpcomingState.UPCOMING_MEETINGS.name());
        InlineKeyboardButton btn2 = meetingKeyboard.defaultInlineButton("Отменить встречу",
                UpcomingState.UPCOMING_CANCEL_BY_OWNER.name() + " " + meetingDto.getId());

        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessageService.getMessage("upcoming.meeting.selected.owner"),
                meetingKeyboard.buildInlineMarkup(List.of(List.of(btn1, btn2))));
        executeSendMessage(sendMessage);
    }

    public void sendConfirmedComingMeeting(Meeting meeting) {
        Set<AccountMeeting> accountMeetings = meeting.getAccountMeetings();
        ZonedDateTime zonedDateTime = meeting.getDate();
        for (AccountMeeting accountMeeting : accountMeetings) {
            Account account = accountMeeting.getAccount();
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
        Set<AccountMeeting> accountMeetings = meeting.getAccountMeetings();
        AccountDto accountDto = accountMapper.map(meeting.getOwner());
        String ownerLink = messageUtils.generateAccountLink(accountDto, "", "");
        String text = localeMessageService.getMessage("upcoming.meeting.notification.comment", ownerLink);

        InlineKeyboardButton skip = meetingKeyboard.defaultInlineButton("Пропустить", " ");
        for (AccountMeeting accountMeeting : accountMeetings) {
            Account participant = accountMeeting.getAccount();
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(participant.getId(), text,
                    meetingKeyboard.buildInlineMarkup(List.of(List.of(skip))));
            executeSendMessage(sendMessage);
        }
    }

    public void sendParticipantSelectionForPing(long userId, MeetingDto meetingDto) {
        Set<AccountDto> participants = meetingDto.getParticipantsWithoutOwner();
        String message = localeMessageService.getMessage("upcoming.meeting.ping.select");
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (AccountDto participant : participants) {
            InlineKeyboardButton button = meetingKeyboard.defaultInlineButton(participant.getFirstname(),
                    UpcomingState.UPCOMING_SEND_NOTIFICATION_PARTICIPANT +
                            " " + meetingDto.getId() + " " + participant.getId());
            keyboard.add(List.of(button));
        }

        InlineKeyboardButton btn5 = meetingKeyboard.defaultInlineButton(Emojis.LEFT.getEmojiSpace() + "Назад",
                UpcomingState.UPCOMING_SELECTED_MEETING + " " + meetingDto.getId());
        keyboard.add(List.of(btn5));
        EditMessageText editMessageText = messageUtils.generateEditMessageHtml(userId, message,
                meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditMessage(editMessageText);
    }

    public void sendPingParticipant(long userId, long participantId, MeetingDto meetingDto) {
        Account account = accountService.getByUserId(participantId)
                .orElseThrow(()-> new AccountNotFoundException(userId));
        AccountDto accountDto = accountMapper.map(account);
        String zoneId = accountDto.getTimeZone();
        ZonedDateTime zonedDateTime = meetingDto.getDateWithZoneId(zoneId);

        String message = localeMessageService.getMessage("upcoming.meeting.ping.notification",
                zonedDateTime.format(CustomFormatter.DATE_TIME_FORMATTER));

        InlineKeyboardButton btn = meetingKeyboard.defaultInlineButton("Установить статус",
                UpcomingState.UPCOMING_SELECTED_MEETING + " " + meetingDto.getId());

        SendMessage sendMessage = messageUtils.generateSendMessage(participantId, message,
                meetingKeyboard.buildInlineMarkup(List.of(List.of(btn))));
        executeSendMessage(sendMessage);

        String participantLink = messageUtils.generateAccountLink(accountDto, "", "");
        EditMessageText editMessageText = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("upcoming.meeting.ping.success", participantLink), null);
        executeMessage(editMessageText);
    }

    public void sendReadyMeeting(MeetingDto meetingDto) {
        Set<AccountDto> participants = meetingDto.getParticipants();
        ZonedDateTime readyTime = meetingDto.getDate();
        String ownerLink = messageUtils.generateAccountLink(meetingDto.getOwner(), "", "");

        for (AccountDto participant : participants) {
            String zoneId = participant.getTimeZone();
            String date = readyTime.withZoneSameInstant(ZoneId.of(zoneId))
                    .format(CustomFormatter.DATE_TIME_WEEK_FORMATTER);

            SendMessage sendMessage =
                    messageUtils.generateSendMessageHtml(participant.getId(),
                            localeMessageService.getMessage("upcoming.meeting.confirmed.ready",
                                    ownerLink, Emojis.CALENDAR.getEmojiSpace() + date,
                                    Emojis.OFFICE.getEmojiSpace() + meetingDto.getAddress()), null);

            executeSendMessage(sendMessage);
        }
    }
}
