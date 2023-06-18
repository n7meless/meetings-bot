package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
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
    @CreatedDate
    private LocalDateTime createdDt;
    @Fetch(FetchMode.JOIN)
    @OneToOne(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    private Settings settings;
    @Fetch(FetchMode.JOIN)
    @OneToOne(mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    private BotState botState;
    @ManyToMany(mappedBy = "participants")
    private List<Meeting> meetings;
    @ManyToMany
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"))
    private List<Group> groups;

    @OneToMany(mappedBy = "account")
    private List<AccountTime> meetingTimes;

    public void addMeeting(Meeting meeting){
        this.meetings.add(meeting);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
