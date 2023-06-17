package com.ufanet.meetingsbot.handler.keyboard;

import com.ufanet.meetingsbot.constants.state.AccountState;
import com.ufanet.meetingsbot.dto.UpdateDto;
import com.ufanet.meetingsbot.service.UpdateService;
import com.ufanet.meetingsbot.service.message.UpcomingMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.ufanet.meetingsbot.constants.ReplyKeyboardButton.UPCOMING_MEETINGS;

@Component
@RequiredArgsConstructor
public class UpcomingKeyboardHandler implements KeyboardHandler {
    private final UpcomingMessageService upcomingMessageService;
    @Override
    public void handleUpdate(Update update) {
        UpdateDto updateDto = UpdateService.parseUpdate(update);
        String content = updateDto.content();
        long userId = updateDto.chatId();
        if (content.equals(UPCOMING_MEETINGS.getButtonName())){
            upcomingMessageService.sendUpcomingMeetings(userId);
        }
    }

    @Override
    public AccountState getUserStateHandler() {
        return AccountState.UPCOMING;
    }
}
