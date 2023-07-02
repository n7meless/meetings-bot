package com.ufanet.meetingsbot.cache;

public interface Cache<T> {
    void save(Long userId, T state);

    T get(Long userId);

    void evict(Long userId);
}
