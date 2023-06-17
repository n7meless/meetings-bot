package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    @OneToMany(mappedBy = "meetingDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MeetingTime> meetingTimes;

    public Set<MeetingTime> getMeetingTimes() {
        return meetingTimes == null ? new HashSet<>() : meetingTimes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingDate date = (MeetingDate) o;
        return Objects.equals(date.getDate(), this.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
