package com.ufanet.meetingsbot.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Getter
public class NullCallbackException extends ResponseStatusException {
    private final String callbackId;

    public NullCallbackException(String callbackId) {
        super(HttpStatus.OK);
        this.callbackId = callbackId;
    }
}
