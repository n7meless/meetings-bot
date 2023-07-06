package com.ufanet.meetingsbot.entity;

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
@NamedEntityGraph(name = "meeting-with-children", attributeNodes = {
        @NamedAttributeNode(value = "dates", subgraph = "dates.meetingTimes"),
        @NamedAttributeNode(value = "subject", subgraph = "subject.questions"),
        @NamedAttributeNode(value = "participants", subgraph = "participants.settings"),
        @NamedAttributeNode(value = "group")},
        subgraphs = {
                @NamedSubgraph(name = "subject.questions",
                        attributeNodes = @NamedAttributeNode(value = "questions")),
                @NamedSubgraph(name = "dates.meetingTimes",
                        attributeNodes = @NamedAttributeNode(value = "meetingTimes",
                                subgraph = "meetingTimes.accountTimes")),
                @NamedSubgraph(name = "meetingTimes.accountTimes",
                        attributeNodes = @NamedAttributeNode(value = "accountTimes")),
                @NamedSubgraph(name = "participants.settings",
                        attributeNodes = @NamedAttributeNode(value = "settings"))
        })
public class Meeting implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Account owner;

    @ManyToMany
    @JoinTable(name = "user_meetings",
            joinColumns = @JoinColumn(name = "meeting_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<Account> participants;

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

    @OneToMany(mappedBy = "meeting", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<MeetingDate> dates;

    public ZonedDateTime getDate() {
        return this.dates.stream().findFirst()
                .map(MeetingDate::getMeetingTimes)
                .get().stream().findFirst().get().getDateTime();
    }
}
