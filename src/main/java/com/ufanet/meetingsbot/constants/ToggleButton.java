package com.ufanet.meetingsbot.constants;

public enum ToggleButton {
    SEND, CANCEL, NEXT, PREV;

    public static ToggleButton typeOf(String button) {
        for (ToggleButton btn : ToggleButton.values()) {
            if (btn.name().equals(button)) return btn;
        }
        return null;
    }
}
