package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.state.MeetingState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "meetings")
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "owner_id", referencedColumnName = "id")
    private Account owner;
    @ManyToMany(mappedBy = "meetings")
    private List<Account> accounts;
    private String address;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @CreationTimestamp
    private LocalDateTime createdDt;
    @OneToOne(mappedBy = "meeting", cascade = CascadeType.ALL)
    private Subject subject;
    @Enumerated(EnumType.STRING)
    private MeetingState state;
}
