package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.DataCache;
import com.ufanet.meetingsbot.model.Meeting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MeetingCacheManager implements DataCache<Meeting> {
    private final Map<Long, Meeting> meetingDataCache = new HashMap<>();

    @Override
    public void saveData(Long userId, Meeting meeting) {
        meetingDataCache.put(userId, meeting);
    }

    @Override
    public Meeting getData(Long userId) {
        if (meetingDataCache.containsKey(userId)) {
            return meetingDataCache.get(userId);
        }
        return null;
    }

    @Override
    public void clearData(Long userId) {
        if (meetingDataCache.containsKey(userId)) {
            meetingDataCache.remove(userId);
        }
    }

    public Map<Long, Meeting> getMeetingDataCache() {
        return meetingDataCache;
    }
}
