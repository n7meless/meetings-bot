package com.ufanet.meetingsbot.message;

import com.ufanet.meetingsbot.constants.Emojis;
import com.ufanet.meetingsbot.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Set;

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

    public void sendPressButtonMessage(Set<AccountDto> members, long chatId) {

        InlineKeyboardButton pressButton =
                InlineKeyboardButton.builder().text(Emojis.ROCKET.getEmojiSpace() + "Я здесь")
                        .callbackData("REMEMBER").build();
        String membersText;
        if (members.isEmpty()) {
            membersText = localeMessageService.getMessage("group.members.notexists");
        } else {
            String memberLinks = messageUtils.generateAccountLink(members, Emojis.GREEN_SELECTED.getEmojiSpace(), "");
            membersText = localeMessageService.getMessage("group.members.exists", memberLinks);
        }
        String headerText = localeMessageService.getMessage("group.command.start", membersText);
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboardRow(List.of(pressButton)).build();
        SendMessage sendMessage = messageUtils.generateSendMessageHtml(chatId,
                headerText, markup);
        safeExecute(sendMessage);
    }

    public void sendEditPressButtonMessage(Set<AccountDto> members, long chatId, int messageId) {
        String memberLinks = messageUtils.generateAccountLink(members, Emojis.GREEN_SELECTED.getEmojiSpace(), "");

        InlineKeyboardButton pressButton = InlineKeyboardButton.builder().text(Emojis.ROCKET.getEmojiSpace() + "Я здесь")
                .callbackData("REMEMBER").build();

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboardRow(List.of(pressButton)).build();
        String membersText = localeMessageService.getMessage("group.members.exists", memberLinks);
        EditMessageText editMessage = messageUtils.generateEditMessageHtml(chatId, messageId,
                localeMessageService.getMessage("group.command.start", membersText), markup);
        safeExecute(editMessage);
    }

    public void sendNoPermission(long chatId) {
        SendMessage sendMessage =
                messageUtils.generateSendMessage(chatId,
                        localeMessageService.getMessage("group.command.nopermission", Emojis.WARNING.getEmojiSpace()));
        safeExecute(sendMessage);
    }

    public ChatMember getChatMember(long chatId, long userId) throws TelegramApiException {
        GetChatMember getChatMember = new GetChatMember(String.valueOf(chatId), userId);
        return absSender.execute(getChatMember);
    }
}
