package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.model.Account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
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
