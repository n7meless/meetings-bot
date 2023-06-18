package com.ufanet.meetingsbot.handler.chat;

import com.ufanet.meetingsbot.handler.type.ChatType;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.service.GroupService;
import com.ufanet.meetingsbot.service.message.GroupMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GroupChatHandler implements ChatHandler {

    private final GroupService groupService;
    private final GroupMessageService groupReplyMessageHandler;

    @Override
    public void chatUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Chat chat = message.getChat();
            long chatId = chat.getId();

            //TODO если удаляем то не сохранять
            if (chatCreated(message)) {
                //TODO если в группе людей больше то отправляем сообщение
//                groupReplyMessageHandler.sendWelcomeChatMessage(chatId);
                User tgUser = message.getFrom();
                Group group = groupService.saveTgChat(message.getChat());
                groupService.saveMembers(group, List.of(tgUser));
            }
            if (hasMemberUpdate(message)) {
                Optional<Group> optionalGroup = groupService.getByChatId(chatId);
                Group group = optionalGroup.orElseGet(() -> groupService.saveTgChat(chat));
                handleMembers(group, message);
            }
        }
    }

    private boolean chatCreated(Message message) {
        if (message.getGroupchatCreated() != null) {
            return message.getGroupchatCreated();
        } else if (message.getSuperGroupCreated() != null) {
            return message.getSuperGroupCreated();
        }
        return false;
    }

    private boolean hasMemberUpdate(Message message) {
        return message.getLeftChatMember() != null || !message.getNewChatMembers().isEmpty();
    }

    private void handleMembers(Group group, Message message) {
        List<User> newMembers = message.getNewChatMembers();
        User leftMember = message.getLeftChatMember();
        if (!newMembers.isEmpty()) {
            groupService.saveMembers(group, newMembers);
        } else if (leftMember != null && !leftMember.getIsBot()) {
            groupService.removeMember(group, leftMember);
        }
    }

    @Override
    public ChatType getMessageType() {
        return ChatType.GROUP;
    }
}
