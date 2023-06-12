package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.DataCache;
import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MeetingCacheManager implements DataCache<MeetingDto, MeetingState> {
    private final Map<Long, MeetingDto> meetingDataCache = new HashMap<>();
    private final Map<Long, MeetingState> meetingStateCache = new HashMap<>();

    @Override
    public void saveData(Long userId, MeetingDto meetingDto) {
        meetingDataCache.put(userId, meetingDto);
    }

    @Override
    public MeetingDto getData(Long userId) {
        if (meetingDataCache.containsKey(userId)) {
            return meetingDataCache.get(userId);
        }
        return null;
    }

    @Override
    public void clearData(Long userId) {
        if (meetingDataCache.containsKey(userId)) {
            meetingDataCache.remove(userId);
            evict(userId);
        }
    }

    @Override
    public void put(Long userId, MeetingState state) {
        meetingStateCache.put(userId, state);
    }

    @Override
    public MeetingState get(Long userId) {
        return meetingStateCache.getOrDefault(userId, MeetingState.GROUP_SELECTION);
    }

    @Override
    public void evict(Long userId) {
        meetingStateCache.remove(userId);
    }
    //TODO if will be CANCEL state or APPROVED state?
    public void setNextState(Long userId) {
        MeetingState prev = meetingStateCache.get(userId);
        MeetingState[] values = MeetingState.values();
        int ordinal = prev.ordinal();
        MeetingState next = values[ordinal + 1];
        put(userId, next);
    }
}
