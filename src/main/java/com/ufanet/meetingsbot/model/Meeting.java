package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.state.MeetingState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "meetings")
@NamedEntityGraph(name = "meeting-entity-graph", attributeNodes = {
        @NamedAttributeNode(value = "dates", subgraph = "dates.meetingTime"),
        @NamedAttributeNode(value = "subject", subgraph = "subject.questions"),
        @NamedAttributeNode(value = "owner"),
        @NamedAttributeNode(value = "accountMeetings", subgraph = "accountMeetings.account")},
        subgraphs = {
                @NamedSubgraph(name = "subject.questions",
                        attributeNodes = @NamedAttributeNode(value = "questions")),
                @NamedSubgraph(name = "dates.meetingTime",
                        attributeNodes = @NamedAttributeNode(value = "meetingTimes",
                                subgraph = "meetingTimes.accountTimes")),
                @NamedSubgraph(name = "meetingTimes.accountTimes",
                        attributeNodes = @NamedAttributeNode(value = "accountTimes")),
                @NamedSubgraph(name = "accountMeetings.account",
                        attributeNodes = @NamedAttributeNode(value = "account"))})
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
    private LocalDateTime createdDt;
    @Column(name = "updated_dt")
    private LocalDateTime updatedDt;
    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Subject subject;
    @Enumerated(EnumType.STRING)
    private MeetingState state;
    @OrderBy("date")
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MeetingDate> dates;

    public void setSubject(Subject subject) {
        this.subject = subject;
        subject.setMeeting(this);
    }

    public void addMeetingDate(MeetingDate meetingDate) {
        this.dates.add(meetingDate);
    }

    public List<AccountTime> getAccountTimes(Predicate<? super AccountTime> predicate) {
        return this.dates.stream().map(MeetingDate::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(MeetingTime::getAccountTimes).flatMap(Collection::stream)
                .filter(predicate).toList();
    }


    public Set<Account> getParticipants() {
        return this.getAccountMeetings().stream()
                .map(AccountMeeting::getAccount).collect(Collectors.toSet());
    }

    public Set<Account> getParticipantsWithoutOwner() {
        return this.getAccountMeetings().stream()
                .filter(am -> am.getAccount().getId() != this.owner.getId())
                .map(AccountMeeting::getAccount).collect(Collectors.toSet());
    }

    public ZonedDateTime getDate() {
        return this.dates.stream().findFirst()
                .map(MeetingDate::getMeetingTimes)
                .get().stream().findFirst().get().getDateTime();
    }

    public List<ZonedDateTime> getDatesWithZoneId(String zoneId) {
        return this.dates.stream()
                .map(MeetingDate::getMeetingTimes)
                .flatMap(Collection::stream)
                .map(meetingTime -> meetingTime.getTimeWithZoneOffset(zoneId))
                .sorted().toList();
    }

    public void removeDateIf(Predicate<? super MeetingDate> predicate) {
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

    @Override
    public int compareTo(Meeting meeting) {
        return this.state.compareTo(meeting.getState());
    }
}
