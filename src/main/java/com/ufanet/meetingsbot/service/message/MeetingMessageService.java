package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.GroupRepository;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingMessageService extends MessageService {
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final AccountService accountService;
    private final GroupRepository groupRepository;
    private final GroupService groupService;

    public void sendMessage(long userId, Meeting meeting, String callback) {
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECT -> sendGroupMessage(userId, meeting);
            case PARTICIPANT_SELECT -> sendParticipantsMessage(userId, meeting);
            case SUBJECT_SELECT -> sendSubjectMessage(userId, meeting);
            case SUBJECT_DURATION_SELECT -> sendSubjectDurationMessage(userId, meeting);
            case QUESTION_SELECT -> sendQuestionMessage(userId, meeting);
            case DATE_SELECT -> sendDateMessage(userId, meeting, callback);
            case TIME_SELECT -> sendTimeMessage(userId, meeting);
            case ADDRESS_SELECT -> sendAddressMessage(userId, meeting);
            case READY -> sendAwaitingMessage(userId, meeting);
            case CANCELED -> sendCanceledMessage(userId);
        }
    }

    public void sendGroupMessage(long userId, Meeting meeting) {
        List<Group> groups = groupService.getGroupsByMemberId(userId);

        SendMessage message = SendMessage.builder().chatId(userId)
                .text(localeMessageService.getMessage("reply.meeting.group"))
                .replyMarkup(meetingInlineKeyboardMaker.getGroupsInlineMarkup(meeting, groups))
                .build();

        executeSendMessage(message);
    }

    public void sendParticipantsMessage(long userId, Meeting meeting) {
        //TODO caching group
        Set<Account> members = accountService.getAccountByGroupsIdAndIdNot(meeting.getGroup().getId(), userId);

        Set<Account> participants = meeting.getParticipants();
        EditMessageText message = EditMessageText.builder()
                .chatId(userId)
                .text(localeMessageService.getMessage("reply.meeting.participants"))
                .replyMarkup(meetingInlineKeyboardMaker.getParticipantsInlineMarkup(members, participants))
                .build();


        executeEditMessage(message);
    }

    public void sendSubjectMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        InlineKeyboardMarkup keyboardMarkup;
        String title = subject.getTitle();
        boolean hasTitle = title != null;
        String text = "";
        keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(meetingInlineKeyboardMaker.defaultRowHelperInlineMarkup(hasTitle, false))
                .build();
        if (hasTitle) {
            text = "Текущая тема: " + title;
            SendMessage sendMessage = messageUtils.generateSendMessage(userId, text,
                    keyboardMarkup);
            executeSendMessage(sendMessage);
        } else {
            text = text + "\nНапишите тему встречи";
            EditMessageText sendMessage =
                    EditMessageText.builder().chatId(userId)
                            .text(localeMessageService.getMessage("reply.meeting.subject"))
                            .replyMarkup(keyboardMarkup)
                            .build();

            executeEditMessage(sendMessage);
        }
    }

    public void sendQuestionMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        Set<Question> questions = subject.getQuestions();
        String text = "Введите вопросы, которые будут обсуждаться на встрече";
        if (questions.size() > 0) {
            text = text + "\nСледующие вопросы будут включены в обсуждение (нажмите для удаления)";
        }
        SendMessage sendMessage =
                messageUtils.generateSendMessage(userId, text,
                        meetingInlineKeyboardMaker.getQuestionsInlineMarkup(meeting));
        executeSendMessage(sendMessage);
    }

    public void sendDateMessage(long userId, Meeting meeting, String callback) {

        EditMessageText message = EditMessageText.builder()
                .text(localeMessageService.getMessage("reply.meeting.date"))
                .chatId(userId)
                .replyMarkup(meetingInlineKeyboardMaker.getCalendarInlineMarkup(meeting, callback))
                .build();

        executeEditMessage(message);

    }

    public void sendTimeMessage(long userId, Meeting meeting) {
        EditMessageText message = EditMessageText.builder()
                .chatId(userId)
                .text(localeMessageService.getMessage("reply.meeting.time"))
                .replyMarkup(meetingInlineKeyboardMaker.getTimeInlineMarkup(meeting)).build();

        executeEditMessage(message);
    }

    public void sendAddressMessage(long userId, Meeting meeting) {

        List<InlineKeyboardButton> buttons =
                meetingInlineKeyboardMaker.defaultRowHelperInlineMarkup(true, true);
        EditMessageText message =
                EditMessageText.builder().chatId(userId).text(localeMessageService.getMessage("reply.meeting.address"))
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboardRow(buttons).build())
                        .build();

        executeEditMessage(message);
    }

    public void sendSubjectDurationMessage(long userId, Meeting meeting) {

        EditMessageText sendMessage =
                EditMessageText.builder()
                        .text(localeMessageService.getMessage("reply.meeting.duration"))
                        .chatId(userId)
                        .replyMarkup(meetingInlineKeyboardMaker.getSubjectDurationInlineMarkup(meeting)).build();


        executeEditMessage(sendMessage);
    }

    public void sendCanceledMessage(long userId) {
        EditMessageText message = EditMessageText.builder().chatId(userId)
                .text("Встреча была отменена!").build();


        executeEditMessage(message);
    }

    public void sendEditErrorMessage(long userId) {
        SendMessage message =
                messageUtils.generateSendMessage(userId, "У вас нет ни одной созданной встречи", null);
        executeSendMessage(message);
    }

    public void sendSuccessMessageParticipants(long userId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessageService.getMessage("reply.meeting.sent.participants"));
        executeSendMessage(sendMessage);
    }

    public void sendSuccessMeetingConfirm(long userId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessageService.getMessage("reply.meeting.confirmed.message"));
        executeSendMessage(sendMessage);
    }

    public void sendAwaitingMessage(long userId, Meeting meeting) {
        InlineKeyboardButton sendButton = InlineKeyboardButton.builder().callbackData(ToggleButton.SEND.name())
                .text("Отправить запрос участникам").build();

        List<List<InlineKeyboardButton>> keyboard = meetingInlineKeyboardMaker.getEditingInlineButtons();
        keyboard.add(0, List.of(sendButton));

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard).build();

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);

        String textMeeting =
                localeMessageService.getMessage("reply.meeting.awaiting",
                        meetingMessage.participants(), meetingMessage.subject(), meetingMessage.questions(),
                        meetingMessage.duration(), meetingMessage.times(), meetingMessage.address());

        SendMessage sendMessage = SendMessage.builder().chatId(userId).parseMode("HTML")
                .replyMarkup(keyboardMarkup).disableWebPagePreview(true)
                .text(textMeeting).build();

        executeSendMessage(sendMessage);
    }

    public void sendMeetingToParticipants(Meeting meeting) {
        Set<Account> participants = meeting.getParticipants();
        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);

        String textMeeting = localeMessageService.getMessage("reply.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());

        Set<AccountTime> accountTimes = meeting.getDates().stream()
                .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                .map(MeetingTime::getAccountTimes).flatMap(Collection::stream)
                .collect(Collectors.toSet());

        for (Account participant : participants) {
            InlineKeyboardMarkup keyboard =
                    meetingInlineKeyboardMaker.getMeetingConfirmKeyboard(meeting);

            SendMessage sendMessage = SendMessage.builder().text(textMeeting).parseMode("HTML")
                    .disableWebPagePreview(true).replyMarkup(keyboard).protectContent(true)
                    .chatId(participant.getId()).build();
            executeSendMessage(sendMessage);
        }
    }

    public void sendMeetingToParticipant(long userId, Meeting meeting) {

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);
        String textMeeting = localeMessageService.getMessage("reply.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());


        InlineKeyboardMarkup keyboard = meetingInlineKeyboardMaker.getMeetingConfirmKeyboard(meeting);
        EditMessageText sendMessage = EditMessageText.builder().text(textMeeting).parseMode("HTML")
                .disableWebPagePreview(true).replyMarkup(keyboard)
                .chatId(userId).build();

        executeEditMessage(sendMessage);
    }

    public void editMeetingToParticipant(long userId, Meeting meeting, List<AccountTime> accountTimes) {

        MeetingMessage meetingMessage = messageUtils.generateMeetingMessage(meeting);
        String textMeeting = localeMessageService.getMessage("reply.meeting.awaiting.participants",
                meetingMessage.owner(), meetingMessage.subject(), meetingMessage.questions(),
                meetingMessage.participants(), meetingMessage.duration(), meetingMessage.times(),
                meetingMessage.address());


        InlineKeyboardMarkup keyboard =
                meetingInlineKeyboardMaker.getChangeMeetingTimeKeyboard(meeting.getId(), accountTimes);
        EditMessageText sendMessage = EditMessageText.builder().text(textMeeting).parseMode("HTML")
                .disableWebPagePreview(true).replyMarkup(keyboard)
                .chatId(userId).build();

        executeEditMessage(sendMessage);
    }

    public void sendCanceledAccountTimeMessage(Meeting meeting) {
        Account owner = meeting.getOwner();
        String ownerLink = "<a href='https://t.me/" + owner.getUsername() + "'>" + owner.getFirstname() + "</a>";
        Set<Account> participants = meeting.getParticipants();
        participants.add(owner);
        for (Account participant : participants) {
            SendMessage sendMessage = SendMessage.builder().chatId(participant.getId())
                    .text(localeMessageService.getMessage("reply.meeting.canceled.participants.message",
                            ownerLink)).parseMode("HTML").disableWebPagePreview(true)
                    .build();
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
        String text = "Время запланировано на " + localDateTime.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER);

        for (Account participant : participants) {
            SendMessage sendMessage = SendMessage.builder().text(text).chatId(participant.getId()).build();
            executeSendMessage(sendMessage);
        }
    }
}
