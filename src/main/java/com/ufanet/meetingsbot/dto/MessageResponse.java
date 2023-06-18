package com.ufanet.meetingsbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record MessageResponse(@JsonProperty(value = "ok")
                              boolean success,
                              @JsonProperty(value = "message_id")
                              int messageId) implements Serializable {
}
