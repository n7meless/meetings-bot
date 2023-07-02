package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "meeting_time")
public class MeetingTime implements Comparable<MeetingTime>, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date_time")
    private ZonedDateTime dateTime;
    @ManyToOne
    @JoinColumn(name = "date_id", referencedColumnName = "id")
    private MeetingDate meetingDate;
    @OneToMany(mappedBy = "meetingTime", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @Fetch(FetchMode.JOIN)
    private Set<AccountTime> accountTimes;

    @Override
    public int compareTo(MeetingTime meetingTime) {
        return this.getDateTime().compareTo(meetingTime.getDateTime());
    }

    public ZonedDateTime getTimeWithZoneOffset(String zoneId) {
        return this.dateTime.withZoneSameInstant(ZoneId.of(zoneId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingTime that = (MeetingTime) o;
        return this.getDateTime().isEqual(that.getDateTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime);
    }
}
