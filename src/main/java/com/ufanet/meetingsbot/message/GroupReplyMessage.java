package com.ufanet.meetingsbot.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMemberCount;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class GroupReplyMessage extends ReplyMessage {

    public void sendHelpMessage(long chatId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(chatId,
                localeMessageService.getMessage("group.command.help"));
        safeExecute(sendMessage);
    }

    public void sendAboutMessage(long chatId) {
        SendMessage sendMessage = messageUtils.generateSendMessage(chatId,
                localeMessageService.getMessage("group.command.about"));
        safeExecute(sendMessage);
    }

    public Integer sendGetChatMemberCount(long chatId) {
        GetChatMemberCount getChatMemberCount =
                GetChatMemberCount.builder().chatId(chatId).build();
        try {
            return absSender.execute(getChatMemberCount);
        } catch (TelegramApiException e) {
            return null;
        }
    }
}
