package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "meetings")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Account owner;
    @ManyToMany(cascade = {CascadeType.MERGE})
    @JoinTable(name = "user_meetings",
            joinColumns = @JoinColumn(name = "meeting_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<Account> participants;
    private String address;
    @OneToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @Column(name = "created_dt")
    private LocalDateTime createdDt;
    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL)
    private Subject subject;
    @Enumerated(EnumType.STRING)
    private MeetingState state;
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MeetingDate> dates;


    public List<LocalDateTime> convertMeetingDates(){
        return this.dates.stream()
                .map(MeetingDate::getMeetingTimes).flatMap(Collection::stream)
                .map(MeetingTime::getTime)
                .sorted(LocalDateTime::compareTo).toList();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return Objects.equals(id, meeting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
