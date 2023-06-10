package com.ufanet.meetingsbot.handler.update;

import com.ufanet.meetingsbot.handler.type.HandlerType;
import com.ufanet.meetingsbot.cache.impl.MeetingCacheManager;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.handler.message.CreatingMeetingMessageHandler;
import com.ufanet.meetingsbot.service.MeetingDtoService;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CreateUpdateHandler implements UpdateHandler {
    private final MeetingDtoService meetingDtoService;
    private final MeetingCacheManager cacheManager;
    private final CreatingMeetingMessageHandler creatingMeetingMessageHandler;

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
            default -> meetingDtoService.updateDto(state,  meetingDto, callback);
        }
        creatingMeetingMessageHandler.handleMessage(userId, state, meetingDto);
        return null;
    }

    public void updateMeeting(long userId, String callback, MeetingDto meetingDto) {
        MeetingState state = cacheManager.get(userId);
        switch (state) {
            case GROUP_SELECTION -> meetingDtoService.updateGroup(meetingDto, Long.valueOf(callback));
            case PARTICIPANTS_SELECTION -> meetingDtoService.updateParticipants(meetingDto, Long.valueOf(callback));
            case SUBJECT_SELECTION -> meetingDtoService.updateSubject(meetingDto, callback);
            case QUESTION_SELECTION -> meetingDtoService.updateQuestion(meetingDto, callback);
            case DATE_SELECTION -> meetingDtoService.updateDate(meetingDto, callback);
            case TIME_SELECTION -> meetingDtoService.updateTime(meetingDto, callback);
            case ADDRESS_SELECTION -> {
                meetingDtoService.updateAddress(meetingDto, callback);
                cacheManager.clearData(userId);
            }
        }
    }

    @Override
    public HandlerType getTypeHandler() {
        return HandlerType.CREATE;
    }
}
