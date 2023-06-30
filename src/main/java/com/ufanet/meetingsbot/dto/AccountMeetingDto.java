package com.ufanet.meetingsbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountMeetingDto {
    private Long id;
    private AccountDto account;
    private String comment;
    private Integer rate;
}
