package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.MessageType;

public record LastMessage(int messageId, MessageType type) {}
