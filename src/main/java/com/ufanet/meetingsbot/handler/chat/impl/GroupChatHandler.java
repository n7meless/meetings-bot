package com.ufanet.meetingsbot.handler.chat.impl;

import com.ufanet.meetingsbot.constants.type.ChatType;
import com.ufanet.meetingsbot.dto.AccountDto;
import com.ufanet.meetingsbot.entity.Account;
import com.ufanet.meetingsbot.entity.Group;
import com.ufanet.meetingsbot.handler.chat.ChatHandler;
import com.ufanet.meetingsbot.mapper.AccountMapper;
import com.ufanet.meetingsbot.mapper.GroupMapper;
import com.ufanet.meetingsbot.message.GroupReplyMessage;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupChatHandler implements ChatHandler {

    private final GroupReplyMessage groupMessage;
    private final GroupService groupService;
    private final AccountService accountService;

    @Override
    public void handleChatUpdate(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }
    }

    protected void handleCallback(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Message message = callbackQuery.getMessage();
        Long chatId = message.getChatId();
        User user = callbackQuery.getFrom();
        Integer messageId = message.getMessageId();

        log.info("handle callback in chat {} from user {}", chatId, user.getId());

        if (data.equals("REMEMBER")) {
            Group group = groupService.getByGroupId(message.getChatId())
                    .orElseGet(() ->
                            GroupMapper.MAPPER.map(message.getChat()));

            Set<AccountDto> members = group.getMembers().stream()
                    .map(AccountMapper.MAPPER::map)
                    .collect(Collectors.toSet());
            AccountDto accountDto = AccountMapper.MAPPER.mapToDtoFromTgUser(user);

            if (members.contains(accountDto)) {
                groupMessage.executeNullCallback(callbackQuery.getId());
            } else {
                Account account = accountService.getByUserId(user.getId())
                        .orElseGet(() -> accountService.createAccount(user));

                group.addMember(account);
                groupService.save(group);

                members.add(accountDto);
                groupMessage.sendEditPressButtonMessage(members, chatId, messageId);
            }
        }
    }

    protected void handleMessage(Message message) {
        if (message.hasText() && message.getText().startsWith("/")) {
            handleCommand(message);
        } else if (isChatCreated(message) || hasMemberUpdate(message)) {
            handleNewAndLeftMembers(message);
        }
    }

    protected void handleStartCommand(Message message) {
        Long chatId = message.getChatId();
        User from = message.getFrom();
        log.info("handle start command in chat {} from user {}", chatId, from.getId());
        try {
            ChatMember chatMember = groupMessage.getChatMember(chatId, from.getId());
            String memberStatus = chatMember.getStatus();
            if (memberStatus.equals("creator") || memberStatus.equals("administrator")) {
                Group group = groupService.getByGroupId(chatId).orElseGet(() -> {
                    Group newGroup = GroupMapper.MAPPER.map(message.getChat());
                    return groupService.save(newGroup);
                });
                Set<AccountDto> members = group.getMembers().stream().map(AccountMapper.MAPPER::map)
                        .collect(Collectors.toSet());
                groupMessage.sendPressButtonMessage(members, chatId);
            } else {
                groupMessage.sendNoPermission(chatId);
            }
        } catch (TelegramApiException e) {
            log.error("an occurred error when get from telegram chat {} member {}", chatId, from.getId());
        }
    }


    protected void handleNewAndLeftMembers(Message message) {
        List<User> newMembers = message.getNewChatMembers();
        if (!newMembers.isEmpty()) {
            log.info("handle new members from chat {}", message.getChatId());

            Group group = groupService.getByGroupId(message.getChatId()).orElseGet(() -> {
                newMembers.add(message.getFrom());
                return GroupMapper.MAPPER.map(message.getChat());
            });

            for (User member : newMembers) {
                if (!member.getIsBot()) {

                    Long userId = member.getId();
                    Account account = accountService.getByUserId(userId)
                            .orElseGet(() -> accountService.createAccount(member));
                    group.addMember(account);
                }
            }
            groupService.save(group);
        } else {
            log.info("handle left members from chat {}", message.getChatId());

            User leftMember = message.getLeftChatMember();
            if (leftMember != null && !leftMember.getIsBot()) {
                Optional<Group> group = groupService.getByGroupId(message.getChatId());
                if (group.isEmpty()) return;
                groupService.removeMember(group.get(), leftMember);
            }
        }
    }

    protected void handleCommand(Message message) {
        long chatId = message.getChat().getId();
        String messageText = message.getText();
        log.info("handle command in group chat {}", chatId);

        if (messageText.startsWith("/start")) {
            handleStartCommand(message);
        } else if (messageText.startsWith("/help")) {
            groupMessage.sendHelpMessage(chatId);
        } else if (messageText.startsWith("/about")) {
            groupMessage.sendAboutMessage(chatId);
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

    @Override
    public ChatType getChatType() {
        return ChatType.GROUP;
    }
}
