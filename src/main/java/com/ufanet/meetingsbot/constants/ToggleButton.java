package com.ufanet.meetingsbot.constants;

public enum ToggleButton {
    CURRENT, SEND, CANCEL, NEXT, PREV, READY;

    public static ToggleButton typeOf(String button) {
        for (ToggleButton btn : ToggleButton.values()) {
            if (btn.name().equals(button)) return btn;
        }
        return CURRENT;
    }
}
