package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.state.AccountState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.EntityGraph;

import java.io.Serial;
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
@NamedEntityGraph(name = "account_with_settings", attributeNodes = {
        @NamedAttributeNode(value = "settings"),
        @NamedAttributeNode(value = "botState"),
})
@NamedEntityGraph(name = "accounts_with_settings_and_botstate",attributeNodes = {
        @NamedAttributeNode(value = "groups"),
        @NamedAttributeNode(value = "settings"),
        @NamedAttributeNode(value = "botState")
})
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
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    @Fetch(FetchMode.JOIN)
    private Settings settings;
    @Fetch(FetchMode.JOIN)
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
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
    public void updateBotState(AccountState state){
        this.getBotState().setState(state);
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
