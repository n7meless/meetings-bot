package com.ufanet.meetingsbot.botapi.handlers.callbackquery;

import com.ufanet.meetingsbot.botapi.handlers.type.HandlerType;
import com.ufanet.meetingsbot.cache.MeetingCacheManager;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.service.MeetingDtoCreator;
import com.ufanet.meetingsbot.service.PrivateMessageProcessor;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CreateUpdateHandler implements UpdateHandler {
    private final MeetingDtoCreator meetingDtoCreator;
    private final MeetingCacheManager cacheManager;
    private final PrivateMessageProcessor privateMessageProcessor;

    @Override
    public BotApiMethod<?> handleUpdate(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        long userId = callbackQuery.getFrom().getId();
        MeetingDto meetingDto = cacheManager.getData(userId);
        MeetingState state = cacheManager.get(userId);
        String callback = callbackQuery.getData();
        switch (callback) {
            case "next" -> cacheManager.setNextState(userId);
            case "cancel" -> cacheManager.clearData(userId);
            default -> updateMeeting(userId, callback, meetingDto);
        }
        privateMessageProcessor.sendMessage(userId, state, meetingDto);
        return null;
    }

    public void updateMeeting(long userId, String callback, MeetingDto meetingDto) {
        MeetingState state = cacheManager.get(userId);
        switch (state) {
            case GROUP_SELECTION -> meetingDtoCreator.updateGroup(meetingDto, Long.valueOf(callback));
            case PARTICIPANTS_SELECTION -> meetingDtoCreator.updateParticipants(meetingDto, Long.valueOf(callback));
            case SUBJECT_SELECTION -> meetingDtoCreator.updateSubject(meetingDto, callback);
            case QUESTION_SELECTION -> meetingDtoCreator.updateQuestion(meetingDto, callback);
            case DATE_SELECTION -> meetingDtoCreator.updateDate(meetingDto, callback);
            case TIME_SELECTION -> meetingDtoCreator.updateTime(meetingDto, callback);
            case ADDRESS_SELECTION -> {
                meetingDtoCreator.updateAddress(meetingDto, callback);
                cacheManager.clearData(userId);
            }
        }
    }

    @Override
    public HandlerType getTypeHandler() {
        return HandlerType.CREATE;
    }
}
