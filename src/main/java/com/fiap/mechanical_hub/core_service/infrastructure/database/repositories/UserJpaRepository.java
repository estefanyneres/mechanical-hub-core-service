package com.fiap.mechanical_hub.core_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.core_service.infrastructure.database.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserModel, UUID> {
    UserModel findByEmail(String email);
}
