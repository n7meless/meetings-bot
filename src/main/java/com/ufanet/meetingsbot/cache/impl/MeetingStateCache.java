package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.model.Meeting;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class MeetingStateCache implements Cache<Meeting> {

    private final Map<Long, Meeting> meetingStateCache = new HashMap<>();

    @Override
    public void save(Long userId, Meeting meeting) {
        log.info("saving MeetingDto in cache by user {}", userId);
        meetingStateCache.put(userId, meeting);
    }

    @Override
    public Meeting get(Long userId) {
        log.info("getting MeetingDto from cache by user {}", userId);
        return meetingStateCache.getOrDefault(userId, null);
    }

    @Override
    public void evict(Long userId) {
        log.info("evict MeetingDto cache by user {}", userId);
        meetingStateCache.remove(userId);
    }
}
