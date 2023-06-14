package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.*;
import com.ufanet.meetingsbot.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MeetingReplyMessageService extends ReplyMessageService {
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final AccountRepository accountRepository;


    public void sendGroupMessage(long userId, Meeting meeting) {

        Account account = accountRepository.findById(userId).orElseGet(Account::new);
        List<Group> groups = account.getGroups();

        SendMessage message = messageUtils.generateSendMessage(userId, "Выберите группу",
                 meetingInlineKeyboardMaker.getGroupsInlineMarkup(groups));
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

    public void sendSubjectMessage(long userId) {
        Integer messageId = messageCache.get(userId);
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(meetingInlineKeyboardMaker.defaultRowHelperInlineMarkup(false)).build();
        EditMessageText sendMessage = messageUtils.generateEditMessage(userId, "Напишите тему встречи",
                messageId, keyboardMarkup);
        telegramBot.safeExecute(sendMessage);
    }

    public void sendQuestionMessage(long userId, Meeting meeting) {
        Subject subject = meeting.getSubject();
        List<Question> questions = subject.getQuestions();
        SendMessage sendMessage =
                messageUtils.generateSendMessage(userId, "Введите вопросы, которые будут обсуждаться на встрече",
                         meetingInlineKeyboardMaker.getQuestionsInlineMarkup(questions));
        disableInlineLastMessage(userId);
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
                messageId, meetingInlineKeyboardMaker.getTimeInlineMarkup(meeting.getDates()));
        telegramBot.safeExecute(message);
    }

    public void sendAddressMessage(long userId, Meeting meeting) {
        Integer messageId = messageCache.get(userId);

        EditMessageText message = messageUtils.generateEditMessage(userId, "Напишите адрес где будет проводиться встреча",
                messageId, null);
        telegramBot.safeExecute(message);
    }

    public void sendAwaitingMessage(long userId, Meeting meeting) {
        SendMessage sendMessage = messageUtils.generateMeeting(userId,meeting, meetingInlineKeyboardMaker.getEditingInlineMarkup());
        Message message = (Message) telegramBot.safeExecute(sendMessage);
        messageCache.put(userId, message.getMessageId());
    }
    public void sendSubjectDurationMessage(long userId){
        SendMessage sendMessage = messageUtils.generateSendMessage(userId,
                "Выбрите предполагаемую продолжительность темы в минутах", meetingInlineKeyboardMaker.getSubjectDurationInlineMarkup());
        disableInlineLastMessage(userId);
        Message response = (Message) telegramBot.safeExecute(sendMessage);
        messageCache.put(userId, response.getMessageId());
    }
    public void sendCanceledMessage(long userId) {
        Integer messageId = messageCache.get(userId);
        EditMessageText message = messageUtils.generateEditMessage(userId, "Встреча была отменена!",
                messageId, null);
        telegramBot.safeExecute(message);
        disableInlineLastMessage(userId);
    }
}
