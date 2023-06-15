package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "meeting_date")
public class MeetingDate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @ManyToOne
    @JoinColumn(name = "meeting_id", referencedColumnName = "id")
    private Meeting meeting;
    @OneToMany(mappedBy = "date", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<MeetingTime> time;

    public List<MeetingTime> getTime() {
        return time == null ? new ArrayList<>() : time;
    }
}
