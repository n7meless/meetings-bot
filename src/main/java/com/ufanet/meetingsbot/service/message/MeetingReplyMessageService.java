package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MeetingReplyMessageService extends ReplyMessageService {
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final AccountRepository accountRepository;

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
        Account account = accountRepository.findById(userId).orElseGet(Account::new);
        List<Group> groups = account.getGroups();

        SendMessage message = messageUtils.generateSendMessage(userId, "Выберите группу",
                meetingInlineKeyboardMaker.getGroupsInlineMarkup(meeting, groups));
        disableInlineLastMessage(userId);
        Message response = (Message) telegramBot.safeExecute(message);
        messageCache.put(userId, response.getMessageId());
    }

    public void sendParticipantsMessage(long userId, Meeting meeting) {
        Integer messageId = messageCache.get(userId);
        Set<Account> members = accountRepository.findAccountByGroupsIdAndIdNot(meeting.getGroup().getId(), userId);
        Set<Account> participants = meeting.getParticipants();
        EditMessageText message = messageUtils.generateEditMessage(userId, "Выберите участников",
                messageId, meetingInlineKeyboardMaker.getParticipantsInlineMarkup(members, participants));
        telegramBot.safeExecute(message);
    }

    public void sendSubjectMessage(long userId, Meeting meeting) {
        Integer messageId = messageCache.get(userId);
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
            deleteLastMessage(userId);
            Message message = (Message) telegramBot.safeExecute(sendMessage);
            messageCache.put(userId, message.getMessageId());
        }else {
            text = text + "\nНапишите тему встречи";
            EditMessageText sendMessage = messageUtils.generateEditMessage(userId, text,
                    messageId, keyboardMarkup);
            telegramBot.safeExecute(sendMessage);
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
        deleteLastMessage(userId);
        Message response = (Message) telegramBot.safeExecute(sendMessage);
        messageCache.put(userId, response.getMessageId());
    }

    public void sendDateMessage(long userId, Meeting meeting, String callback) {
        Integer messageId = messageCache.get(userId);

        List<MeetingDate> dates = meeting.getDates();
        EditMessageText message = messageUtils.generateEditMessage(userId, "Выберите дату",
                messageId, meetingInlineKeyboardMaker.getCalendarInlineMarkup(dates, callback));
        telegramBot.safeExecute(message);
    }

    public void sendTimeMessage(long userId, Meeting meeting) {
        Integer messageId = messageCache.get(userId);

        EditMessageText message = messageUtils.generateEditMessage(userId, "Выберите время",
                messageId, meetingInlineKeyboardMaker.getTimeInlineMarkup(new ArrayList<>(meeting.getDates())));
        telegramBot.safeExecute(message);
    }

    public void sendAddressMessage(long userId, Meeting meeting) {
        Integer messageId = messageCache.get(userId);

        List<InlineKeyboardButton> buttons = meetingInlineKeyboardMaker.defaultRowHelperInlineMarkup(true, true);
        EditMessageText message = messageUtils.generateEditMessage(userId, "Напишите адрес где будет проводиться встреча",
                messageId, InlineKeyboardMarkup.builder().keyboardRow(buttons).build());
        telegramBot.safeExecute(message);
    }

    public void sendAwaitingMessage(long userId, Meeting meeting) {
        InlineKeyboardButton sendButton = InlineKeyboardButton.builder().callbackData("SEND")
                .text("Отправить запрос участникам").build();

        List<List<InlineKeyboardButton>> keyboard = meetingInlineKeyboardMaker.getEditingInlineButtons();
        keyboard.add(0, List.of(sendButton));

        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard).build();
        String text = messageUtils.generateAwaitingMeeting(meeting);
        SendMessage sendMessage = SendMessage.builder().chatId(userId).parseMode("MarkdownV2").replyMarkup(keyboardMarkup).disableWebPagePreview(true).text(text).build();
        deleteLastMessage(userId);
        Message message = (Message) telegramBot.safeExecute(sendMessage);
        messageCache.put(userId, message.getMessageId());
    }

    public void sendSubjectDurationMessage(long userId, Meeting meeting) {
        Integer messageId = messageCache.get(userId);

        String message = "Выбрите предполагаемую продолжительность темы в минутах";

            EditMessageText sendMessage = messageUtils.generateEditMessage(userId,
                    message, messageId, meetingInlineKeyboardMaker.getSubjectDurationInlineMarkup(meeting));
            telegramBot.safeExecute(sendMessage);
    }

    public void sendCanceledMessage(long userId) {
        Integer messageId = messageCache.get(userId);
        EditMessageText message = messageUtils.generateEditMessage(userId, "Встреча была отменена!",
                messageId, null);
        telegramBot.safeExecute(message);
        disableInlineLastMessage(userId);
    }

    public void sendEditErrorMessage(long userId) {
        SendMessage message = messageUtils.generateSendMessage(userId, "У вас нет ни одной созданной встречи", null);
        telegramBot.safeExecute(message);
    }

    public void sendMeetingMessage(long userId, Meeting meeting) {
        Set<Account> participants = meeting.getParticipants();
        String textMeeting = messageUtils.generateTextMeeting(meeting);
        for (Account participant : participants) {
            SendMessage sendMessage = SendMessage.builder().text(textMeeting).parseMode("MarkdownV2").disableWebPagePreview(true).chatId(participant.getId()).build();
            telegramBot.safeExecute(sendMessage);
        }
    }
}
