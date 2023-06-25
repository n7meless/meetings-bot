package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.repository.EntityGraph;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "subject")
@NamedEntityGraph(name = "subject-with-questions",
        attributeNodes = @NamedAttributeNode(value = "questions"))
public class Subject{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
//    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Question> questions;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", referencedColumnName = "id")
    private Meeting meeting;
    private Integer duration;

    public void addQuestion(Question question){
        question.setSubject(this);
        this.questions.add(question);
    }
    public Set<Question> getQuestions() {
        return questions == null ? new HashSet<>() : questions;
    }
}
