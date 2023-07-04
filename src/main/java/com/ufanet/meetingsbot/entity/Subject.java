package com.ufanet.meetingsbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.mapstruct.Mapper;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "subject")
public class Subject implements Serializable {

    @Id
    private Long id;

    private String title;

    @Builder.Default
    @ElementCollection
    @Column(name = "title")
    @CollectionTable(name = "question", joinColumns = @JoinColumn(name = "subject_id"))
    private Set<String> questions = new HashSet<>();

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", referencedColumnName = "id")
    private Meeting meeting;

    private Integer duration;

}
