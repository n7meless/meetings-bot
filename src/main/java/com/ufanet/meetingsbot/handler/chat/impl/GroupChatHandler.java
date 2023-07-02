package com.ufanet.meetingsbot.handler.chat.impl;

import com.ufanet.meetingsbot.constants.type.ChatType;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.mapper.GroupMapper;
import com.ufanet.meetingsbot.model.Group;
import com.ufanet.meetingsbot.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupChatHandler implements ChatHandler {

    private final GroupService groupService;
    private final GroupMapper groupMapper;

    @Override
    public void chatUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            Chat chat = message.getChat();
            long chatId = chat.getId();

            if (isChatCreated(message) || hasMemberUpdate(message)) {
                handleMembers(message);
            }
        }
    }

    private boolean isChatCreated(Message message) {
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

    protected void handleMembers(Message message) {
        List<User> newMembers = message.getNewChatMembers();
        if (isChatCreated(message)) newMembers.add(message.getFrom());

        Group group = groupService.getByGroupId(message.getChatId())
                .orElseGet(() -> groupMapper.map(message.getChat()));

        User leftMember = message.getLeftChatMember();
        if (!newMembers.isEmpty()) {
            groupService.saveMembers(group, newMembers);
        } else if (leftMember != null && !leftMember.getIsBot()) {
            groupService.removeMember(group, leftMember);
        }
    }

    @Override
    public ChatType getChatType() {
        return ChatType.GROUP;
    }
}
