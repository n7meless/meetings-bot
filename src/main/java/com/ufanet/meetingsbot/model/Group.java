package com.ufanet.meetingsbot.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
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
    @ManyToMany(mappedBy = "groups", cascade = CascadeType.ALL)
    private List<Account> members;
}
