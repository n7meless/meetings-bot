package com.ufanet.meetingsbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "chat")
@EqualsAndHashCode(of = {"id"})
@EntityListeners({AuditingEntityListener.class})
public class Group implements Serializable {
    @Id
    private Long id;

    private String title;

    private String description;

    @CreationTimestamp
    @Column(name = "created_dt")
    private LocalDateTime createdDt;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_chats",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<Account> members = new HashSet<>();

    public void addMember(Account account) {
        this.members.add(account);
    }
}
