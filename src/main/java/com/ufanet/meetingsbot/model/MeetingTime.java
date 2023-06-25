package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "meeting_time")
@NamedEntityGraph(name = "meetingtime-with-accounttimes",
        attributeNodes = {
                @NamedAttributeNode(value = "accountTimes", subgraph = "accountTimes.account")
        },
        subgraphs = @NamedSubgraph(name = "accountTimes.account",
                attributeNodes = @NamedAttributeNode(value = "account")))
public class MeetingTime implements Comparable<MeetingTime> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DateTimeFormat(pattern = "dd.MM.yyyy H:mm")
    private LocalDateTime time;
    @ManyToOne
    @JoinColumn(name = "date_id", referencedColumnName = "id")
    private MeetingDate meetingDate;
    @OneToMany(mappedBy = "meetingTime", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @Fetch(FetchMode.JOIN)
    private Set<AccountTime> accountTimes;

    public void addAccountTime(AccountTime accountTime) {
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
