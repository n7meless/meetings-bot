package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.state.AccountState;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.TimeZone;
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "user_settings")
public class Settings implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Account account;
    private TimeZone timeZone;
}
