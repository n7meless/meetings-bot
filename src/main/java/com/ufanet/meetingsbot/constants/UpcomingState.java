package com.ufanet.meetingsbot.constants;

public enum UpcomingState {
    UPCOMING_MEETINGS,
    UPCOMING_EDIT_MEETING_TIME,
    UPCOMING_CONFIRM_MEETING_TIME,
    UPCOMING_CANCEL_MEETING_TIME;
    public static UpcomingState typeOf(String type){
        for (UpcomingState value : UpcomingState.values()) {
            if (value.name().equals(type)) return value;
        }
        return null;
    }
}
