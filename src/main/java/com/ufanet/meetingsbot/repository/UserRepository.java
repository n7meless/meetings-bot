package com.ufanet.meetingsbot.repository;

import com.ufanet.meetingsbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findUserByChatsId(Long chatId);
}
