package com.ufanet.meetingsbot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "chats")
public class Chat {
    @Id
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startedDt;
    @ManyToMany(mappedBy = "chats")
    private List<User> user;
}
