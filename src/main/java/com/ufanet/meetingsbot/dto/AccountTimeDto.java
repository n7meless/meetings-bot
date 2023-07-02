package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.constants.Status;
import lombok.*;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountTimeDto implements Comparable<AccountTimeDto> {
    private Long id;
    private Status status;
    private AccountDto account;
    private MeetingTimeDto meetingTime;

    @Override
    public int compareTo(AccountTimeDto dto) {
        return this.getMeetingTime().getDateTime()
                .compareTo(dto.getMeetingTime().getDateTime());
    }
}
