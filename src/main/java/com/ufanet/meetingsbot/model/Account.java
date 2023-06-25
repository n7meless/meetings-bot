package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
@EqualsAndHashCode(of = {"id"})
@EntityListeners({AuditingEntityListener.class})
@NamedEntityGraph(name = "account_with_settings", attributeNodes = {
        @NamedAttributeNode(value = "settings"),
        @NamedAttributeNode(value = "botState"),
})
@NamedEntityGraph(name = "accounts_with_settings_and_botstate", attributeNodes = {
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
//    @Fetch(FetchMode.JOIN)
    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    private Settings settings;
//    @Fetch(FetchMode.JOIN)
    @OneToOne(fetch = FetchType.LAZY, optional = false, mappedBy = "account", orphanRemoval = true, cascade = CascadeType.ALL)
    private BotState botState;
    @OneToMany(mappedBy = "account", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AccountMeeting> accountMeetings;
    @ManyToMany
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"))
    private List<Group> groups;

    @OneToMany(mappedBy = "account")
    private List<AccountTime> meetingTimes;

    public String getZoneId(){
        return this.settings.getTimeZone();
    }
}
