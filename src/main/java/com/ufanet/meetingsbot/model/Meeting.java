package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@Setter
@Getter
@Entity(name = "meetings")
@AllArgsConstructor
@NoArgsConstructor
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long ownerId;
    @ManyToMany(mappedBy = "meetings")
    private List<User> users;
    private String address;
    @CreationTimestamp
    private LocalDateTime createdDt;
    @OneToOne(cascade = CascadeType.ALL)
    private Subject subject;
}
