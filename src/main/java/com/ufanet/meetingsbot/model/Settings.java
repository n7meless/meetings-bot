package com.ufanet.meetingsbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
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
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    @JoinColumn(name = "user_id")
    private Account account;
    @Column(name = "time_zone")
    private String timeZone;
    private String language;
}
