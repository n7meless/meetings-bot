package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private Long id;
    @Column(name = "first_name")
    private String firstname;
    @Column(name = "last_name")
    private String lastname;
    private String username;
    //    private TimeZone timeZone;
    private String languageCode;
    private LocalDateTime createdDt;
    @ManyToMany
    @JoinTable(name = "user_meetings",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "meeting_id", referencedColumnName = "id"))
    private List<Meeting> meetings;
    @ManyToMany
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"))
    private List<Chat> chats;
}
