package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.dto.MeetingDto;
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
public class MeetingDtoStateCache implements Cache<MeetingDto> {

    private final Map<Long, MeetingDto> meetingStateCache = new HashMap<>();

    @Override
    public void save(Long userId, MeetingDto meeting) {
        log.info("saving MeetingDto in cache by user {}", userId);
        meetingStateCache.put(userId, meeting);
    }

    @Override
    public MeetingDto get(Long userId) {
        log.info("getting MeetingDto from cache by user {}", userId);
        return meetingStateCache.getOrDefault(userId, null);
    }

    @Override
    public void evict(Long userId) {
        log.info("evict MeetingDto cache by user {}", userId);
        meetingStateCache.remove(userId);
    }
}
