package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GroupChatHandler implements ChatHandler {

    private final ChatService chatService;

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Chat chat = message.getChat();
            Long chatId = chat.getId();
            Optional<Group> optionalGroup = chatService.getByChatId(chatId);
            if (optionalGroup.isEmpty()) {
                chatService.saveTgChat(chat);
            } else {
                Group group = optionalGroup.get();
                handleMembers(group, message);
            }
        }
        return null;
    }

    private void handleMembers(Group group, Message message) {
        List<User> newMembers = message.getNewChatMembers();
        User leftMember = message.getLeftChatMember();
        if (!newMembers.isEmpty()) {
            chatService.saveMembers(group, newMembers);
        } else if (leftMember != null && !leftMember.getIsBot()) {
            chatService.removeMember(group, leftMember);
        }
    }

    @Override
    public ChatType getMessageType() {
        return ChatType.GROUP;
    }
}
