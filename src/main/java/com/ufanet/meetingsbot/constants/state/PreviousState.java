package com.ufanet.meetingsbot.constants.state;


public enum PreviousState {
    PREVIOUS_MEETINGS;

    public static PreviousState typeOf(String type) {
        for (PreviousState value : PreviousState.values()) {
            if (value.name().equals(type)) return value;
        }
        return null;
    }
}
