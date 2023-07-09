package com.ufanet.meetingsbot.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class AccountDto {
    private Long id;
    private String firstname;
    private String lastname;
    private String username;
    private String language;
    private String zoneId;
}
