package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.entity.Meeting;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class MeetingCache implements Cache<Meeting> {

    private final Map<Long, Meeting> meetingStateCache = new ConcurrentHashMap<>();

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
