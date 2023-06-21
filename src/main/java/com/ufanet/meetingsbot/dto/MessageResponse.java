package com.ufanet.meetingsbot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(@JsonProperty(value = "ok")
                              boolean success) implements Serializable {
}
