package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.dto.UpdateDto;
import org.telegram.telegrambots.meta.api.objects.*;

public class UpdateService {
    public static UpdateDto parseUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();
            String chatType = message.getChat().getType();
            return new UpdateDto(chatId, text, chatType);
        } else {
            CallbackQuery query = update.getCallbackQuery();
            Message message = query.getMessage();
            Long chatId = message.getChatId();
            String data = query.getData();
            String chatType = message.getChat().getType();
            return new UpdateDto(chatId, data, chatType);
        }
    }
    public User getLeftChatMembers(Update update){
        if (update.hasChatMember()){
            ChatMemberUpdated chatMember = update.getChatMember();
        };
        return null;
    }
}
