package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity(name = "questions")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @ManyToOne
    private Subject subject;
}
