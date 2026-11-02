package com.fiap.mechanical_hub.core_service.infrastructure.database.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@AllArgsConstructor
public class ProfileModel {

    public ProfileModel() {
    }

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column()
    private String description;

}
