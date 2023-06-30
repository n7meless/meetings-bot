package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.Status;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_times")
public class AccountTime implements Comparable<AccountTime>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_time_id", referencedColumnName = "id")
    private MeetingTime meetingTime;

    @Override
    public int compareTo(AccountTime accountTime) {
        return this.getMeetingTime().compareTo(accountTime.getMeetingTime());
    }
}
