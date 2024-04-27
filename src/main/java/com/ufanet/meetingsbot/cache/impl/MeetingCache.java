package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.entity.Meeting;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
@RequiredArgsConstructor
public class MeetingCache implements Cache<Meeting> {

    private final Map<Long, Meeting> meetingCache = new ConcurrentHashMap<>();

    @Override
    public void save(Long userId, Meeting meeting) {
        meetingCache.put(userId, meeting);
    }

    @Override
    public Meeting get(Long userId) {
        return meetingCache.getOrDefault(userId, null);
    }

    @Override
    public void evict(Long userId) {
        meetingCache.remove(userId);
    }
}
