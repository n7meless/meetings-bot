package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.botapi.TelegramBot;
import com.ufanet.meetingsbot.cache.BotMessageCache;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.keyboard.InlineKeyboardMaker;
import com.ufanet.meetingsbot.model.Chat;
import com.ufanet.meetingsbot.model.User;
import com.ufanet.meetingsbot.repository.UserRepository;
import com.ufanet.meetingsbot.state.MeetingState;
import com.ufanet.meetingsbot.utils.MessageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;

@Service
public class PrivateMessageProcessor {
    private final MessageUtils messageUtils;
    private final BotMessageCache botMessageCache;
    private final InlineKeyboardMaker inlineKeyboardMaker;
    private final UserRepository userRepository;
    private final TelegramBot telegramBot;
    @Autowired
    public PrivateMessageProcessor(MessageUtils messageUtils, BotMessageCache botMessageCache,
                                   InlineKeyboardMaker inlineKeyboardMaker, UserRepository userRepository,
                                   @Lazy TelegramBot telegramBot) {
        this.messageUtils = messageUtils;
        this.botMessageCache = botMessageCache;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.userRepository = userRepository;
        this.telegramBot = telegramBot;
    }

    public BotApiMethod<?> sendMessage(long userId, MeetingState state, MeetingDto meetingDto) {
        switch (state) {
            case GROUP_SELECTION -> {
                EditMessageText message = processGroupMessage(userId, meetingDto);
            }
            case PARTICIPANTS_SELECTION -> {
                EditMessageText message = processParticipantsMessage(userId, meetingDto);
            }
            case SUBJECT_SELECTION -> processSubjectMessage(userId, meetingDto);
            case QUESTION_SELECTION -> {
                processSubjectMessage(userId, meetingDto);
            }
            case DATE_SELECTION -> {
                processDateMessage(userId, meetingDto);
            }
            case TIME_SELECTION -> {
                processTimeMessage(userId, meetingDto);
            }
            case ADDRESS_SELECTION -> {
                processAddressMessage(userId, meetingDto);
            }
        }
        return null;
    }


    private EditMessageText processGroupMessage(long userId, MeetingDto meetingDto) {
        Integer messageId = botMessageCache.get(userId);

        User user = userRepository.findById(userId).orElseGet(User::new);
        List<Chat> chats = user.getChats();

        return messageUtils.generateEditMessageWithText(userId, "",
                messageId, inlineKeyboardMaker.getGroupsInlineMarkup(chats));
    }

    private EditMessageText processParticipantsMessage(long userId, MeetingDto meetingDto) {
        Integer messageId = botMessageCache.get(userId);
        List<User> participants = userRepository.findUserByChatsId(userId);

        return messageUtils.generateEditMessageWithText(userId, "",
                messageId, inlineKeyboardMaker.getParticipantsInlineMarkup(participants));
    }

    private SendMessage processSubjectMessage(long userId, MeetingDto meetingDto) {
        List<User> participants = userRepository.findUserByChatsId(userId);

        return messageUtils.generateSendMessageWithText(userId, "",
                inlineKeyboardMaker.getParticipantsInlineMarkup(participants));
    }

    private SendMessage processQuestionMessage(long userId, MeetingDto meetingDto) {
        List<User> participants = userRepository.findUserByChatsId(userId);

        return messageUtils.generateSendMessageWithText(userId, "",
                inlineKeyboardMaker.getParticipantsInlineMarkup(participants));
    }

    private SendMessage processDateMessage(long userId, MeetingDto meetingDto) {

        return messageUtils.generateSendMessageWithText(userId, "",
                inlineKeyboardMaker.getCalendarInlineMarkup());
    }

    private EditMessageText processTimeMessage(long userId, MeetingDto meetingDto) {
        Integer messageId = botMessageCache.get(userId);

        return messageUtils.generateEditMessageWithText(userId, "",
                messageId, inlineKeyboardMaker.getTimeOfDiscussionInlineMarkup());
    }

    private EditMessageText processAddressMessage(long userId, MeetingDto meetingDto) {
        Integer messageId = botMessageCache.get(userId);

        return messageUtils.generateEditMessageWithText(userId, "",
                messageId, inlineKeyboardMaker.getTimeOfDiscussionInlineMarkup());
    }
}
