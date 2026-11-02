package com.fiap.mechanical_hub.core_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ProfileModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileJpaRepository extends JpaRepository<ProfileModel, UUID> {
    ProfileModel findByName(String name);
}
