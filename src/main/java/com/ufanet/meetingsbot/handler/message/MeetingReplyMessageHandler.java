package com.ufanet.meetingsbot.handler.message;

import com.ufanet.meetingsbot.cache.impl.BotMessageCache;
import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.repository.AccountRepository;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.state.MeetingState;
import com.ufanet.meetingsbot.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;

@Service
public class MeetingReplyMessageHandler extends ReplyMessageHandler {
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final AccountRepository accountRepository;
    private final MeetingService meetingService;

    @Autowired
    public MeetingReplyMessageHandler(BotMessageCache messageCache,
                                      MessageUtils messageUtils, MeetingInlineKeyboardMaker meetingInlineKeyboardMaker,
                                      AccountRepository accountRepository, MeetingService meetingService) {
        super(messageCache, messageUtils);
        this.meetingInlineKeyboardMaker = meetingInlineKeyboardMaker;
        this.accountRepository = accountRepository;
        this.meetingService = meetingService;
    }

    @Override
    public void handle(long userId, String message) {
        Meeting meeting = meetingService.getMeetingByOwnerId(userId);
        MeetingState state = meeting.getState();
        switch (state) {
            case GROUP_SELECTION -> sendGroupMessage(userId);
            case PARTICIPANTS_SELECTION -> sendParticipantsMessage(userId);
            case SUBJECT_SELECTION -> sendSubjectMessage(userId);
            case QUESTION_SELECTION -> sendQuestionMessage(userId);
            case DATE_SELECTION -> sendDateMessage(userId);
            case TIME_SELECTION -> sendTimeMessage(userId);
            case ADDRESS_SELECTION -> sendAddressMessage(userId);
        }
    }


    private void sendGroupMessage(long userId) {
        Integer messageId = messageCache.get(userId);

        Account account = accountRepository.findById(userId).orElseGet(Account::new);
        List<Group> groups = account.getGroups();

        EditMessageText message = messageUtils.generateSendMessage(userId, "",
                messageId, meetingInlineKeyboardMaker.getGroupsInlineMarkup(groups));
        telegramBot.safeExecute(message);
    }

    private void sendParticipantsMessage(long userId) {
        Integer messageId = messageCache.get(userId);
        List<Account> participants = accountRepository.findAccountByGroupsId(userId);

        EditMessageText message = messageUtils.generateSendMessage(userId, "",
                messageId, meetingInlineKeyboardMaker.getParticipantsInlineMarkup(participants));
        telegramBot.safeExecute(message);
    }

    private void sendSubjectMessage(long userId) {
        List<Account> participants = accountRepository.findAccountByGroupsId(userId);

        SendMessage message = messageUtils.generateSendMessage(userId, "",
                meetingInlineKeyboardMaker.getParticipantsInlineMarkup(participants));
        telegramBot.safeExecute(message);
    }

    private void sendQuestionMessage(long userId) {
        List<Account> participants = accountRepository.findAccountByGroupsId(userId);

        SendMessage message = messageUtils.generateSendMessage(userId, "",
                meetingInlineKeyboardMaker.getParticipantsInlineMarkup(participants));
        telegramBot.safeExecute(message);
    }

    private void sendDateMessage(long userId) {

        SendMessage message = messageUtils.generateSendMessage(userId, "",
                meetingInlineKeyboardMaker.getCalendarInlineMarkup());
        telegramBot.safeExecute(message);
    }

    private void sendTimeMessage(long userId) {
        Integer messageId = messageCache.get(userId);

        EditMessageText message = messageUtils.generateSendMessage(userId, "",
                messageId, meetingInlineKeyboardMaker.getTimeOfDiscussionInlineMarkup());
        telegramBot.safeExecute(message);
    }

    private void sendAddressMessage(long userId) {
        Integer messageId = messageCache.get(userId);

        EditMessageText message = messageUtils.generateSendMessage(userId, "",
                messageId, meetingInlineKeyboardMaker.getTimeOfDiscussionInlineMarkup());
        telegramBot.safeExecute(message);
    }

}
