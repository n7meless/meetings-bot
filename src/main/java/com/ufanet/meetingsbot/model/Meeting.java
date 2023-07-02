package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "meeting")
@NamedEntityGraph(name = "meeting-entity-graph", attributeNodes = {
        @NamedAttributeNode(value = "dates", subgraph = "dates.meetingTimes"),
        @NamedAttributeNode(value = "subject", subgraph = "subject.questions"),
        @NamedAttributeNode(value = "owner"),
        @NamedAttributeNode(value = "group"),
        @NamedAttributeNode(value = "accountMeetings", subgraph = "accountMeetings.account")},
        subgraphs = {
                @NamedSubgraph(name = "subject.questions",
                        attributeNodes = @NamedAttributeNode(value = "questions")),
                @NamedSubgraph(name = "dates.meetingTimes",
                        attributeNodes = @NamedAttributeNode(value = "meetingTimes", subgraph = "meetingTimes.accountTimes")),
                @NamedSubgraph(name = "meetingTimes.accountTimes",
                        attributeNodes = @NamedAttributeNode(value = "accountTimes")),
                @NamedSubgraph(name = "accountMeetings.account",
                        attributeNodes = @NamedAttributeNode(value = "account", subgraph = "account.settings")),
                @NamedSubgraph(name = "account.settings", attributeNodes = @NamedAttributeNode(value = "settings"))
        })
public class Meeting implements Comparable<Meeting>, Serializable {
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
    private LocalDateTime createdDt;

    @Column(name = "updated_dt")
    private LocalDateTime updatedDt;

    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Subject subject;

    @Enumerated(EnumType.STRING)
    private MeetingState state;

    @OneToMany(mappedBy = "meeting", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Meeting meeting) {
        return this.state.compareTo(meeting.getState());
    }
}
