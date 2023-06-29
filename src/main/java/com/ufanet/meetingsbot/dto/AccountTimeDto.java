package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.Status;
import com.ufanet.meetingsbot.model.Account;
import com.ufanet.meetingsbot.model.MeetingTime;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountTimeDto {

    private Long id;
    private Status status;
    private AccountDto account;
    private MeetingTimeDto meetingTime;
}
