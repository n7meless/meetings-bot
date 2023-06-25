package com.ufanet.meetingsbot.service;

import com.ufanet.meetingsbot.dto.UpdateDto;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.*;
@Service
public class UpdateService {
    public UpdateDto parseUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();
            String chatType = message.getChat().getType();
            return new UpdateDto(chatId, text, chatType);
        } else if (update.hasCallbackQuery()){
            CallbackQuery query = update.getCallbackQuery();
            Message message = query.getMessage();
            Long chatId = message.getChatId();
            String data = query.getData();
            String chatType = message.getChat().getType();
            return new UpdateDto(chatId, data, chatType);
        } else if (update.hasInlineQuery()){
            String chatType = update.getInlineQuery().getChatType();
            String data = update.getInlineQuery().getQuery();
            Long id = update.getInlineQuery().getFrom().getId();
            return new UpdateDto(id, data, chatType);
        }else return null;
    }
    public User getLeftChatMembers(Update update){
        if (update.hasChatMember()){
            ChatMemberUpdated chatMember = update.getChatMember();
        };
        return null;
    }
}
