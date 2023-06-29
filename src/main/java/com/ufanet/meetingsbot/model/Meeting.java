package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "meetings")
@NamedEntityGraph(name = "meeting-entity-graph", attributeNodes = {
//        @NamedAttributeNode(value = "dates", subgraph = "dates.meetingTime"),
//        @NamedAttributeNode(value = "subject", subgraph = "subject.questions"),
        @NamedAttributeNode(value = "owner"),
        @NamedAttributeNode(value = "subject"),
        @NamedAttributeNode(value = "group"),
        @NamedAttributeNode(value = "accountMeetings", subgraph = "accountMeetings.account")},
        subgraphs = {
//                @NamedSubgraph(name = "subject.questions",
//                        attributeNodes = @NamedAttributeNode(value = "questions")),
//                @NamedSubgraph(name = "dates.meetingTime",
//                        attributeNodes = @NamedAttributeNode(value = "meetingTimes",
//                                subgraph = "meetingTimes.accountTimes")),
//                @NamedSubgraph(name = "meetingTimes.accountTimes",
//                        attributeNodes = @NamedAttributeNode(value = "accountTimes")),
                @NamedSubgraph(name = "accountMeetings.account",
                        attributeNodes = @NamedAttributeNode(value = "account", subgraph = "account.settings")),
                @NamedSubgraph(name = "account.settings", attributeNodes = @NamedAttributeNode(value = "settings"))
        })
public class Meeting implements Comparable<Meeting> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Account owner;

    @OneToMany(mappedBy = "meeting", orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<AccountMeeting> accountMeetings;
    private String address;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;

    @Column(name = "created_dt")
    @CreationTimestamp
    private ZonedDateTime createdDt;

    @Column(name = "updated_dt")
    private ZonedDateTime updatedDt;

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    private MeetingState state;

    @OneToMany(mappedBy = "meeting",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MeetingDate> dates;

    public ZonedDateTime getDate() {
        return this.dates.stream().findFirst()
                .map(MeetingDate::getMeetingTimes)
                .get().stream().findFirst().get().getDateTime();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meeting meeting = (Meeting) o;
        return Objects.equals(id, meeting.id);
    }

    public void removeDateIf(Predicate<? super MeetingDate> predicate) {
        this.dates.removeIf(predicate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Meeting meeting) {
        return this.state.compareTo(meeting.getState());
    }
}
