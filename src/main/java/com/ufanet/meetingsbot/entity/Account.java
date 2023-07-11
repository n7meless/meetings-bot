package com.ufanet.meetingsbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
@EqualsAndHashCode(of = {"id"})
@EntityListeners({AuditingEntityListener.class})
public class Account implements Serializable {
    @Id
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstname;

    @Column(name = "last_name")
    private String lastname;

    private String username;

    @CreationTimestamp
    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "account", cascade = CascadeType.ALL)
    private Settings settings;

    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "account", cascade = CascadeType.ALL)
    private BotState botState;

    @ManyToMany(mappedBy = "participants")
    private List<Meeting> meetings;

    @ManyToMany
    @JoinTable(name = "user_chats",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"))
    private List<Group> groups;

    @OneToMany(mappedBy = "account")
    private List<AccountTime> meetingTimes;
}
