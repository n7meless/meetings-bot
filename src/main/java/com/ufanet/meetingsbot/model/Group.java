package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "chats")
public class Group implements Serializable {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String biography;
    private String title;
    private String description;
    private LocalDateTime startedDt;
    @ManyToMany
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    @Fetch(value = FetchMode.JOIN)
    private Set<Account> members;

    public Set<Account> getMembers() {
        return members == null? new HashSet<>() : members;
    }
}
