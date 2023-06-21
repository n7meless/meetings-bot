package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.Status;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "meeting_time")
public class MeetingTime implements Serializable, Comparable<MeetingTime> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime time;
    @ManyToOne
    @JoinColumn(name = "date_id", referencedColumnName = "id")
    private MeetingDate meetingDate;
    @OneToMany(mappedBy = "meetingTime",cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private Set<AccountTime> accountTimes;

    public void addAccountTime(AccountTime accountTime){
        this.accountTimes.add(accountTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeetingTime that = (MeetingTime) o;
        return this.getTime().isEqual(that.getTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(time);
    }

    @Override
    public int compareTo(MeetingTime meetingTime) {
        return this.getTime().compareTo(meetingTime.getTime());
    }
}
