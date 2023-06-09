package com.ufanet.meetingsbot.botapi.handlers;

public interface StateManager {
    void setNextStep(Long userId);
    void setPrevStep(Long userId);
}
