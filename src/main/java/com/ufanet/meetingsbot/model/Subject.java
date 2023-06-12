package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "subject")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;

    @OneToMany(mappedBy = "subject", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Question> questions;

    @OneToOne
    @JoinColumn(name = "meeting_id", referencedColumnName = "id")
    private Meeting meeting;
}
