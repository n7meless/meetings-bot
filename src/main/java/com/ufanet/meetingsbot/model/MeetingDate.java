package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "meeting_date")
public class MeetingDate implements Comparable<MeetingDate> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", referencedColumnName = "id")
    private Meeting meeting;
    @Fetch(FetchMode.JOIN)
    @OrderBy("dateTime")
    @OneToMany(mappedBy = "meetingDate", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MeetingTime> meetingTimes = new HashSet<>();

    public void setDateWithZoneId(String zoneId){
        this.date = LocalDate.ofInstant(Instant.from(this.date), ZoneId.of(zoneId));
    }

    public void addMeetingTime(MeetingTime meetingTime) {

        this.meetingTimes.add(meetingTime);
    }

    public void removeTimeIf(Predicate<? super MeetingTime> predicate) {
        this.meetingTimes.removeIf(predicate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingDate date = (MeetingDate) o;
        return this.getDate().isEqual(date.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public int compareTo(MeetingDate meetingDate) {
        return this.getDate().compareTo(meetingDate.getDate());
    }
}
