package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "subject")
public class Subject{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Size(min = 3, message = "title must be larger than 3 chars")
    private String title;
    @ElementCollection
    @Column(name = "title")
    @CollectionTable(name = "questions", joinColumns = @JoinColumn(name = "subject_id"))
    private Set<String> questions;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", referencedColumnName = "id")
    private Meeting meeting;
    private Integer duration;

    public Set<String> getQuestions() {
        return questions == null ? new HashSet<>() : questions;
    }
}
