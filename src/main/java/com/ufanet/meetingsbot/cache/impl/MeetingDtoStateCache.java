package com.ufanet.meetingsbot.cache.impl;

import com.ufanet.meetingsbot.cache.Cache;
import com.ufanet.meetingsbot.dto.MeetingDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Getter
@Service
@RequiredArgsConstructor
public class MeetingDtoStateCache implements Cache<MeetingDto> {
    private final Map<Long, MeetingDto> meetingStateCache = new HashMap<>();

    @Override
    public void save(Long userId, MeetingDto meeting) {
        meetingStateCache.put(userId, meeting);
    }

    @Override
    public MeetingDto get(Long userId) {
        return meetingStateCache.getOrDefault(userId, null);
    }

    @Override
    public void evict(Long userId) {
        meetingStateCache.remove(userId);
    }
}
