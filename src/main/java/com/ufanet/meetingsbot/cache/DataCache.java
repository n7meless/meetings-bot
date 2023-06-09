package com.ufanet.meetingsbot.cache;

public interface DataCache<C, S> extends Cache<S> {
    void saveData(Long userId, C state);

    C getData(Long userId);

    void clearData(Long userId);
}
