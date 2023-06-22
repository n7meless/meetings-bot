package com.ufanet.meetingsbot.cache;

public interface Cache<C> {
    void save(Long userId, C state);

    C get(Long userId);

    void evict(Long userId);
}
