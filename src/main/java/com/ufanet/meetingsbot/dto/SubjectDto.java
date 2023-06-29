package com.ufanet.meetingsbot.dto;

import com.ufanet.meetingsbot.model.Meeting;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {
    private Long id;
    private String title;
    @Builder.Default
    private Set<String> questions = new HashSet<>();
    private Integer duration;
}
