package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
@EqualsAndHashCode(of = {"id"})
@EntityListeners({AuditingEntityListener.class})
public class Account implements Serializable {
    @Id
    private Long id;

    @Column(name = "first_name")
    private String firstname;

    @Column(name = "last_name")
    private String lastname;

    private String username;

    @Column(name = "created_dt")
    @CreationTimestamp
    private ZonedDateTime createdDt;

    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "account", cascade = CascadeType.ALL)
    private Settings settings;

    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "account", cascade =  CascadeType.ALL)
    private BotState botState;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<AccountMeeting> accountMeetings;

    @ManyToMany
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"))
    private List<Group> groups;

    @OneToMany(mappedBy = "account")
    private List<AccountTime> meetingTimes;

    public String getZoneId() {
        return this.settings.getTimeZone();
    }
}
