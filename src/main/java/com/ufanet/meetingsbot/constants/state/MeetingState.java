package com.ufanet.meetingsbot.constants.state;

public enum MeetingState {
    GROUP_SELECTION,
    PARTICIPANTS_SELECTION,
    SUBJECT_SELECTION,
    SUBJECT_DURATION_SELECTION,
    QUESTION_SELECTION,
    DATE_SELECTION,
    TIME_SELECTION,
    ADDRESS_SELECTION,
    READY,
    SENT,
    AWAITING,
    APPROVED,
    CANCELED;

    public static MeetingState typeOf(String state){
        for (MeetingState meetingState : MeetingState.values()) {
            if (meetingState.name().equals(state)){
                return meetingState;
            }
        }
        return null;
    }
}
