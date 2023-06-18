package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.ToggleButton;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.AccountRepository;
import com.ufanet.meetingsbot.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingMessageService extends MessageService {
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final AccountRepository accountRepository;
    private final GroupRepository groupRepository;

    public void sendMessage(long userId, Meeting meeting, String callback) {
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECTION -> sendGroupMessage(userId, meeting);
            case PARTICIPANTS_SELECTION -> sendParticipantsMessage(userId, meeting);
            case SUBJECT_SELECTION -> sendSubjectMessage(userId, meeting);
            case SUBJECT_DURATION_SELECTION -> sendSubjectDurationMessage(userId, meeting);
            case QUESTION_SELECTION -> sendQuestionMessage(userId, meeting);
            case DATE_SELECTION -> sendDateMessage(userId, meeting, callback);
            case TIME_SELECTION -> sendTimeMessage(userId, meeting);
            case ADDRESS_SELECTION -> sendAddressMessage(userId, meeting);
            case READY -> sendAwaitingMessage(userId, meeting);
            case CANCELED -> sendCanceledMessage(userId);
        }
    }

    public void sendGroupMessage(long userId, Meeting meeting) {
        List<Group> groups = groupRepository.findGroupsByMemberId(userId);

        SendMessage message = SendMessage.builder().chatId(userId)
                .text(localeMessageService.getMessage("reply.meeting.group"))
                .replyMarkup(meetingInlineKeyboardMaker.getGroupsInlineMarkup(meeting, groups))
                .build();

        executeSendMessage(message);
    }

    public void sendParticipantsMessage(long userId, Meeting meeting) {
        //TODO caching group
        Set<Account> members = accountRepository.findAccountByGroupsIdAndIdNot(meeting.getGroup().getId(), userId);
        ;
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
        List<Question> questions = subject.getQuestions();
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

        List<InlineKeyboardButton> buttons = meetingInlineKeyboardMaker.defaultRowHelperInlineMarkup(true, true);
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
        SendMessage message = messageUtils.generateSendMessage(userId, "У вас нет ни одной созданной встречи", null);
        executeSendMessage(message);
    }

    public void sendSuccessMessageParticipants(long userId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                localeMessageService.getMessage("reply.meeting.sent.participants"));
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
}
