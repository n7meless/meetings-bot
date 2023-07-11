package com.ufanet.meetingsbot.entity;

import com.ufanet.meetingsbot.constants.type.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "bot_state")
public class BotState implements Serializable {
    @Id
    private Long id;

    @Column(name = "msg_id")
    private Integer messageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type")
    private MessageType messageType;

    private String state;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account account;


    @Column(name = "msg_from_bot")
    private boolean msgFromBot;

    private LocalDateTime updatedDt;
}
