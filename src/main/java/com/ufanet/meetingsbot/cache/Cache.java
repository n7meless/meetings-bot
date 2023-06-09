package com.ufanet.meetingsbot.cache;

public interface Cache<S> {
     void put(Long userId, S object);

    S get(Long userId);

    void evict(Long userId);
}
