package com.ufanet.meetingsbot.dto;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdDt;
}
