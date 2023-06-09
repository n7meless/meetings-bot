package com.ufanet.meetingsbot.cache;

import com.ufanet.meetingsbot.dto.MeetingDto;
import com.ufanet.meetingsbot.state.MeetingState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MeetingCacheManager implements DataCache<MeetingDto, MeetingState> {
    private final Map<Long, Map<Integer, MeetingDto>> dataCache = new HashMap<>();
    private final Map<Long, MeetingState> stateCache = new HashMap<>();

    @Override
    public void saveData(Long userId, MeetingDto meetingDto) {
        Map<Integer, MeetingDto> cache =
                dataCache.getOrDefault(userId, new HashMap<>());
        MeetingState state = get(userId);
        cache.put(state.ordinal(), meetingDto);
        dataCache.put(userId, cache);
    }

    @Override
    public MeetingDto getData(Long userId) {
        if (dataCache.containsKey(userId)) {
            Map<Integer, MeetingDto> cache =
                    dataCache.get(userId);
            MeetingState state = get(userId);
            return cache.get(state.ordinal() - 1);
        }
        return null;
    }

    @Override
    public void clearData(Long userId) {
        if (dataCache.containsKey(userId)) {
            dataCache.remove(userId);
            evict(userId);
        }
    }

    @Override
    public void put(Long userId, MeetingState state) {
        stateCache.put(userId, state);
    }

    @Override
    public MeetingState get(Long userId) {
        return stateCache.getOrDefault(userId, MeetingState.GROUP_SELECTION);
    }

    @Override
    public void evict(Long userId) {
        stateCache.remove(userId);
    }

    public void setNextState(Long userId) {
        MeetingState prev = stateCache.get(userId);
        MeetingState[] values = MeetingState.values();
        int ordinal = prev.ordinal();
        MeetingState next = values[ordinal + 1];
        put(userId, next);
    }
}
