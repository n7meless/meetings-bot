package com.ufanet.meetingsbot.model;

import com.ufanet.meetingsbot.constants.MessageType;
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

    @Serial
    private static final long serialVersionUID = 1498189718078742438L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "msg_id")
    private Integer messageId;
    @Enumerated(EnumType.STRING)
    @Column(name = "msg_type")
    private MessageType messageType;
    private String state;
    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @MapsId
    @JoinColumn(name = "user_id")
    private Account account;
    @Column(name = "msg_from_user")
    private boolean msgFromUser;

}
