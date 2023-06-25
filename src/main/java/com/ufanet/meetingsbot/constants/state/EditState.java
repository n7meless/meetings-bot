package com.ufanet.meetingsbot.constants.state;

public enum EditState {
    EDIT_GROUP,
    EDIT_PARTICIPANT,
    EDIT_SUBJECT,
    EDIT_SUBJECT_DURATION,
    EDIT_QUESTION,
    EDIT_DATE,
    EDIT_TIME,
    EDIT_ADDRESS;

    public EditState next() {
        int ordinal = this.ordinal();
        EditState[] values = EditState.values();
        return values[ordinal + 1];
    }
}
