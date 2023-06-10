package com.ufanet.meetingsbot.handler.type;

public enum HandlerType {
    CREATE, PROFILE, UPCOMING, EDIT, GROUP;

    public boolean isPrivateHandler() {
        return switch (this) {
            case CREATE, PROFILE, UPCOMING, EDIT -> true;
            default -> false;
        };
    }
}
