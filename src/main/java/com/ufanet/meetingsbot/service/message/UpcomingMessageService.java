package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.MeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpcomingMessageService extends MessageService{
    private final MeetingService meetingService;
    private final AccountService accountService;

    public void sendUpcomingMeetings(long userId){
        List<Meeting> meetings = accountService.getMeetingsByUserId(userId);
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().build();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (Meeting meeting : meetings) {
            InlineKeyboardButton keyboardButton = InlineKeyboardButton.builder().text(meeting.getSubject().getTitle() + meeting.getAddress()).callbackData(" ").build();
            keyboard.add(List.of(keyboardButton));
        }
        markup.setKeyboard(keyboard);
        SendMessage sendMessage = SendMessage.builder().chatId(userId).replyMarkup(markup).text("Предстоящие встречи").build();
        telegramBot.safeExecute(sendMessage);
    }
}
