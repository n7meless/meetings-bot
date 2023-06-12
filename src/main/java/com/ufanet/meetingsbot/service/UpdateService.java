package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.dto.UpdateDto;
import org.telegram.telegrambots.meta.api.objects.*;

public class UpdateService {
    public static UpdateDto parseUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();
            return new UpdateDto(chatId, text);
        } else {
            CallbackQuery query = update.getCallbackQuery();
            Long chatId = query.getMessage().getChatId();
            String data = query.getData();
            return new UpdateDto(chatId, data);
        }
    }
    public User getLeftChatMembers(Update update){
        if (update.hasChatMember()){
            ChatMemberUpdated chatMember = update.getChatMember();
        };
        return null;
    }
}
