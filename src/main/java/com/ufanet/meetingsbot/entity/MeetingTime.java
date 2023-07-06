package com.ufanet.meetingsbot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "meeting_time")
public class MeetingTime implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_time")
    private ZonedDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_id", referencedColumnName = "id")
    private MeetingDate meetingDate;

    @OneToMany(mappedBy = "meetingTime", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<AccountTime> accountTimes;
}
