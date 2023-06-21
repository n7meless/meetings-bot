package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.MessageType;
import com.ufanet.meetingsbot.constants.state.AccountState;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Getter
@Setter
@Entity(name = "bot_state")
@AllArgsConstructor
@NoArgsConstructor
public class BotState implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "msg_id")
    private Integer messageId;
    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type")
    private MessageType messageType;
    @Enumerated(EnumType.STRING)
    private AccountState state;
    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    @JoinColumn(name = "user_id")
    private Account account;
}