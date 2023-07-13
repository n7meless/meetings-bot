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

    public MeetingState next() {
        MeetingState[] values = MeetingState.values();
        int current = this.ordinal();
        return values[current + 1];
    }
}
