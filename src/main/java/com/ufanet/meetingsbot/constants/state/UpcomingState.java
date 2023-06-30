package com.ufanet.meetingsbot.constants.state;

public enum UpcomingState{
    UPCOMING_MEETINGS,
    UPCOMING_SELECTED_MEETING,
    UPCOMING_EDIT_MEETING_TIME,
    UPCOMING_CONFIRM_MEETING_TIME,
    UPCOMING_CANCEL_MEETING_TIME,
    UPCOMING_CANCEL_BY_OWNER,
    UPCOMING_IAMCONFIRM,
    UPCOMING_IAMLATE,
    UPCOMING_IAMREADY,
    UPCOMING_IWILLNOTCOME,
    UPCOMING_SELECT_PARTICIPANT,
    UPCOMING_SEND_NOTIFICATION_PARTICIPANT;

    public static UpcomingState typeOf(String type){
        for (UpcomingState value : UpcomingState.values()) {
            if (value.name().equals(type)) return value;
        }
        return null;
    }

}
