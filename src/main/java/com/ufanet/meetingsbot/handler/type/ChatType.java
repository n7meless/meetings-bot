package com.ufanet.meetingsbot.handler.type;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ChatType{
    PRIVATE("private"), GROUP("group"), SUPERGROUP("supergroup");
    private final String type;
    public static ChatType typeOf(String type){
        for (ChatType value : ChatType.values()) {
            if (value.toString().equals(type)) return value;
        }
        return null;
    }

    @Override
    public String toString() {
        return type;
    }
}
