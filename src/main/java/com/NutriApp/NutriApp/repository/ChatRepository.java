package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessage, Long> {
    // Si tu campo en ChatMessage se llama 'usuario' y es una entidad Usuario:
    List<ChatMessage> findTop20ByUsuarioUsernameOrderByTimestampAsc(String username);
}
