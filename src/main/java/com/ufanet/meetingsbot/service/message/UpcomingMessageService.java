package com.ufanet.meetingsbot.service.message;

import com.ufanet.meetingsbot.constants.UpcomingState;
import com.ufanet.meetingsbot.constants.state.MeetingState;
import com.ufanet.meetingsbot.dto.MeetingMessage;
import com.ufanet.meetingsbot.keyboard.MeetingInlineKeyboardMaker;
import com.ufanet.meetingsbot.model.Meeting;
import com.ufanet.meetingsbot.model.MeetingDate;
import com.ufanet.meetingsbot.model.MeetingTime;
import com.ufanet.meetingsbot.service.AccountService;
import com.ufanet.meetingsbot.service.LocaleMessageService;
import com.ufanet.meetingsbot.service.MeetingService;
import com.ufanet.meetingsbot.utils.CustomFormatter;
import com.ufanet.meetingsbot.utils.Emojis;
import com.ufanet.meetingsbot.utils.MessageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UpcomingMessageService extends MessageService {
    private final MeetingService meetingService;
    private final AccountService accountService;
    private final MeetingInlineKeyboardMaker meetingInlineKeyboardMaker;
    private final MessageUtils messageUtils;
    private final LocaleMessageService localeMessageService;

    public void sendUpcomingMeetings(long userId) {
        List<Meeting> meetings = meetingService.getMeetingsByUserIdAndState(userId, MeetingState.CONFIRMED);

//        MeetingMessage message = messageUtils.generateMeetingMessage(meeting);

//        String textMessage = localeMessageService.getMessage("reply.meeting.upcoming.all", message.subject(),
//                message.questions(), message.participants(), message.duration(),
//                message.address(), meeting.getState().name());

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Meeting meeting : meetings) {
            LocalDateTime dt = meeting.getDates().stream()
                    .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                    .map(MeetingTime::getTime).findFirst().get();

            InlineKeyboardButton k1 = InlineKeyboardButton.builder()
                    .text(Emojis.CALENDAR.getEmojiSpace() + dt.format(CustomFormatter.DATE_TIME_WEEK_FORMATTER))
                    .callbackData(UpcomingState.UPCOMING_MEETINGS.name() + " " + meeting.getId()).build();
            keyboard.add(List.of(k1));
        }
        markup.setKeyboard(keyboard);

        SendMessage sendMessage = SendMessage.builder().chatId(userId)
                .replyMarkup(markup).text("Выберите предстоящие встречи").build();
        telegramBot.safeExecute(sendMessage);
    }

    public void sendUpcomingMeetingsByMeetingId(long userId, long meetingId) {
        Optional<Meeting> optionalMeeting =
                meetingService.getById(meetingId);
        Meeting meeting = optionalMeeting.get();
        MeetingMessage message = messageUtils.generateMeetingMessage(meeting);
        String textMessage = localeMessageService.getMessage("reply.meeting.upcoming.all", message.subject(),
                message.questions(),message.duration(), message.participants(),
                message.address(), meeting.getState().name());

        SendMessage sendMessage = SendMessage.builder().parseMode("HTML")
                .text(textMessage).disableWebPagePreview(true).chatId(userId).build();
        executeSendMessage(sendMessage);
    }
}
