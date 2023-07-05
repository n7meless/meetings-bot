package com.ufanet.meetingsbot.handler.chat.impl;

import com.ufanet.meetingsbot.constants.type.ChatType;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.mapper.GroupMapper;
import com.ufanet.meetingsbot.message.GroupReplyMessage;
import com.ufanet.meetingsbot.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupChatHandler implements ChatHandler {

    private final GroupReplyMessage groupMessage;
    private final GroupService groupService;
    private final GroupMapper groupMapper;

    @Override
    public void handleChatUpdate(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText() && message.getText().startsWith("/")) {
                handleCommand(message);
            } else if (isChatCreated(message) || hasMemberUpdate(message)) {
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
        log.info("handle group members from chat {}", message.getChatId());
        List<User> newMembers = message.getNewChatMembers();
        if (isChatCreated(message)) newMembers.add(message.getFrom());


        Group group = groupService.getByGroupId(message.getChatId()).orElseGet(() -> {
                    Group newGroup = groupMapper.map(message.getChat());
                    return groupService.save(newGroup);
                }
        );
        User leftMember = message.getLeftChatMember();
        if (!newMembers.isEmpty()) {
            groupService.saveMembers(group, newMembers);
        } else if (leftMember != null && !leftMember.getIsBot()) {
            groupService.removeMember(group, leftMember);
        }
    }

    protected void handleCommand(Message message) {
        long chatId = message.getChat().getId();
        String messageText = message.getText();
        log.info("handle group command from chat {}", chatId);

        if (messageText.startsWith("/help")) {
            groupMessage.sendHelpMessage(chatId);
        } else if (messageText.startsWith("/about")) {
            groupMessage.sendAboutMessage(chatId);
        }
    }

    @Override
    public ChatType getChatType() {
        return ChatType.GROUP;
    }
}
