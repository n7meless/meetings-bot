package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.model.Meeting;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Getter
@Service
@RequiredArgsConstructor
public class MeetingStateCache implements Cache<Meeting> {
    private final Map<Long, Meeting> meetingStateCache = new HashMap<>();

    @Override
    public void save(Long userId, Meeting meeting) {
        meetingStateCache.put(userId, meeting);
    }

    @Override
    public Meeting get(Long userId) {
        return meetingStateCache.getOrDefault(userId, null);
    }

    @Override
    public void evict(Long userId) {
        meetingStateCache.remove(userId);
    }
}
