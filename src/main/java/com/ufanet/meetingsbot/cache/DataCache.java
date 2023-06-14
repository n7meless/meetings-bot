package com.ufanet.meetingsbot.cache;

public interface DataCache<C> {
    void saveData(Long userId, C state);

    C getData(Long userId);

    void clearData(Long userId);
}
