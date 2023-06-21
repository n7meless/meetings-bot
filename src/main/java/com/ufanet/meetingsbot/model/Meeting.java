package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "meetings")
@NamedEntityGraph(name = "client_entity-graph", attributeNodes = {
        @NamedAttributeNode(value = "dates", subgraph = "dates.meetingTime"),
        @NamedAttributeNode(value = "subject", subgraph = "subject.questions"),
        @NamedAttributeNode("owner"),
        @NamedAttributeNode("participants")},
        subgraphs = {
                @NamedSubgraph(name = "dates.meetingTime",
                        attributeNodes = @NamedAttributeNode(value = "meetingTimes",
                                subgraph = "meetingTimes.accountTimes")),
                @NamedSubgraph(name = "subject.questions",
                        attributeNodes = @NamedAttributeNode(value = "questions")),
                @NamedSubgraph(name = "meetingTimes.accountTimes",
                        attributeNodes = @NamedAttributeNode(value = "accountTimes"))}
)
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Account owner;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_meetings",
            joinColumns = @JoinColumn(name = "meeting_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<Account> participants;
    private String address;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @Column(name = "created_dt")
    private LocalDateTime createdDt;
    @Column(name = "updated_dt")
    private LocalDateTime updatedDt;
    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subject subject;
    @Enumerated(EnumType.STRING)
    private MeetingState state;
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MeetingDate> dates;

    public void addMeetingDate(MeetingDate meetingDate) {
        this.dates.add(meetingDate);
    }


    public List<AccountTime> getAccountTimeByUserId(long userId) {
        return this.dates.stream().map(MeetingDate::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(MeetingTime::getAccountTimes).flatMap(Collection::stream)
                .filter(at -> at.getAccount().getId() == userId).toList();
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
        subject.setMeeting(this);
    }

    public void removeDateIf(Predicate<? super MeetingDate> predicate){
        this.dates.removeIf(predicate);
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
