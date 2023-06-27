package com.ufanet.meetingsbot.constants.state;

public enum MeetingState {
    GROUP_SELECT,
    PARTICIPANT_SELECT,
    SUBJECT_SELECT,
    SUBJECT_DURATION_SELECT,
    QUESTION_SELECT,
    DATE_SELECT,
    TIME_SELECT,
    ADDRESS_SELECT,
    EDIT,
    AWAITING,
    CONFIRMED,
    PASSED,
    CANCELED;

    public static MeetingState setNextState(MeetingState meetingState) {
        MeetingState[] values = MeetingState.values();
        int current = meetingState.ordinal();
        return values[current + 1];
    }
}
