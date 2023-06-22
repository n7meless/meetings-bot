package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.model.Meeting;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MeetingCacheManager implements Cache<Meeting> {
    private final Map<Long, Meeting> meetingDataCache = new HashMap<>();

    @Override
    public void save(Long userId, Meeting meeting) {
        meetingDataCache.put(userId, meeting);
    }

    @Override
    public Meeting get(Long userId) {
        if (meetingDataCache.containsKey(userId)) {
            return meetingDataCache.get(userId);
        }
        return null;
    }

    @Override
    public void evict(Long userId) {
        if (meetingDataCache.containsKey(userId)) {
            meetingDataCache.remove(userId);
        }
    }

    public Map<Long, Meeting> getMeetingDataCache() {
        return meetingDataCache;
    }
}
