package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.constants.state.PreviousState;
import com.ufanet.meetingsbot.constants.state.UpcomingState;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.dto.AccountTimeDto;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.Meeting;
import com.ufanet.meetingsbot.message.keyboard.MeetingKeyboardMaker;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UpcomingReplyMessage extends ReplyMessage {
    private final MeetingKeyboardMaker meetingKeyboard;

    public void sendUpcomingMeetingsList(long userId, List<MeetingDto> meetings, AccountDto accountDto) {
        String zoneId = accountDto.getTimeZone();

        InlineKeyboardMarkup markup = meetingKeyboard.getUpcomingMeetingsListMarkup(zoneId, meetings);
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("upcoming.meetings.list"), markup);

        executeEditOrSendMessage(editMessage);
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
        executeEditOrSendMessage(editMessage);
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
        executeEditOrSendMessage(editMessage);
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

    public void sendEditMeetingAccountTimes(long userId, MeetingDto meetingDto, AccountDto accountDto,
                                            List<AccountTimeDto> accountTimes) {
        String zoneId = accountDto.getTimeZone();
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

        executeEditOrSendMessage(sendMessage);
    }

    public void sendSelectedAwaitingMeeting(MeetingDto meetingDto, AccountDto accountDto) {
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meetingDto);

        InlineKeyboardMarkup keyboard =
                meetingKeyboard.getMeetingConfirmKeyboard(meetingDto.getId());

        String zoneId = accountDto.getTimeZone();
        List<ZonedDateTime> dates = meetingDto.getDatesWithZoneId(zoneId);
        String datesText = messageUtils.generateDatesText(dates);

        String textMeeting = localeMessageService.getMessage("create.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), datesText,
                meetingMessage.address());

        SendMessage sendMessage =
                messageUtils.generateSendMessageHtml(accountDto.getId(), textMeeting, keyboard);

        executeSendMessage(sendMessage);
    }


    public void sendMeetingsNotExist(long userId) {
        EditMessageText editMessage =
                messageUtils.generateEditMessageHtml(userId,
                        localeMessageService.getMessage("upcoming.meeting.notexist"), null);
        executeEditOrSendMessage(editMessage);
    }

    public void sendCanceledMeetingByOwner(long userId, MeetingDto meetingDto) {
        ZonedDateTime zonedDateTime = meetingDto.getDate();
        String accountLink = messageUtils.generateAccountLink(meetingDto.getOwner(), "", "");

        Set<AccountDto> accountDtos = meetingDto.getParticipantsWithoutOwner();
        for (AccountDto accountDto : accountDtos) {

            String zoneId = accountDto.getTimeZone();
            String dateWithZone = zonedDateTime.withZoneSameInstant(ZoneId.of(zoneId))
                    .format(CustomFormatter.DATE_TIME_FORMATTER);

            String messageToParticipants = localeMessageService.getMessage("upcoming.meeting.canceled.owner",
                    Emojis.CALENDAR.getEmojiSpace() + dateWithZone,
                    accountLink);

            SendMessage sendMessage = messageUtils.generateSendMessageHtml(accountDto.getId(),
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

    public void sendComingMeetingNotifyToParticipants(Meeting meeting, List<Account> accounts, String messageProperty) {
        ZonedDateTime zonedDateTime = meeting.getDate();
        InlineKeyboardButton btn = meetingKeyboard.defaultInlineButton("Установить статус",
                UpcomingState.UPCOMING_SELECTED_MEETING + " " + meeting.getId());
        for (Account account : accounts) {
            String zoneId = account.getSettings().getTimeZone();
            ZonedDateTime accountZoneTime = zonedDateTime.withZoneSameInstant(ZoneId.of(zoneId));
            SendMessage sendMessage = messageUtils.generateSendMessageHtml(account.getId(),
                    localeMessageService.getMessage(messageProperty,
                            Emojis.ALARM_CLOCK.getEmojiSpace() + accountZoneTime.toLocalTime()),
                    meetingKeyboard.buildInlineMarkup(List.of(List.of(btn))));
            executeSendMessage(sendMessage);
        }
    }

    public void sendPassedMeetings(long userId, MeetingDto meetingDto) {
        MeetingMessage message = messageUtils.generateMeetingMessage(meetingDto);
        String participants = messageUtils.generateAccountLink(meetingDto.getParticipants(), Emojis.GREY_SELECTED.getEmojiSpace(), "");
        String textMessage = localeMessageService.getMessage("upcoming.meeting.confirmed.selected",
                message.subject(), message.questions(), message.duration(),
                participants, message.address(), meetingDto.getState().name());
        InlineKeyboardButton previous = meetingKeyboard.getPreviousInlineButton(PreviousState.PREVIOUS_MEETINGS.name());
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId, textMessage,
                meetingKeyboard.buildInlineMarkup(List.of(List.of(previous))));
        executeEditOrSendMessage(editMessage);
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
        executeEditOrSendMessage(editMessageText);
    }

    public void sendPingParticipant(long userId, MeetingDto meetingDto, AccountDto accountDto) {
        String zoneId = accountDto.getTimeZone();
        ZonedDateTime zonedDateTime = meetingDto.getDateWithZoneId(zoneId);

        String message = localeMessageService.getMessage("upcoming.meeting.ping.notification",
                zonedDateTime.format(CustomFormatter.DATE_TIME_FORMATTER));

        InlineKeyboardButton btn = meetingKeyboard.defaultInlineButton("Установить статус",
                UpcomingState.UPCOMING_SELECTED_MEETING + " " + meetingDto.getId());

        SendMessage sendMessage = messageUtils.generateSendMessage(accountDto.getId(), message,
                meetingKeyboard.buildInlineMarkup(List.of(List.of(btn))));
        executeSendMessage(sendMessage);

        String participantLink = messageUtils.generateAccountLink(accountDto, "", "");
        EditMessageText editMessageText = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("upcoming.meeting.ping.success", participantLink), null);
        executeEditOrSendMessage(editMessageText);
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

    public void sendPreviousMeetingsList(long userId, AccountDto accountDto, List<MeetingDto> meetingDtoList) {
        String zoneId = accountDto.getTimeZone();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (MeetingDto meetingDto : meetingDtoList) {
            ZonedDateTime dateWithZoneId = meetingDto.getDateWithZoneId(zoneId);
            String formatted = CustomFormatter.DATE_TIME_FORMATTER.format(dateWithZoneId);
            InlineKeyboardButton btn =
                    meetingKeyboard.defaultInlineButton(Emojis.CALENDAR.getEmojiSpace() + formatted,
                            PreviousState.PREVIOUS_MEETINGS + " " + meetingDto.getId());
            keyboard.add(List.of(btn));
        }
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                "previous.meeting.choose", meetingKeyboard.buildInlineMarkup(keyboard));
        executeEditOrSendMessage(editMessage);
    }

    public void sendPreviousMeetingsNotExists(long userId) {
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(userId,
                localeMessageService.getMessage("previous.meeting.notexists"), null);
        executeEditOrSendMessage(editMessage);
    }
}
