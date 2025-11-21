package com.NutriApp.NutriApp.repository;

import com.NutriApp.NutriApp.modelo.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationCode, Long> {
    Optional<EmailVerificationCode> findByEmail(String email);
}

