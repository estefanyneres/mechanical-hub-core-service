package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;

import java.util.UUID;

public class Profile {

    private UUID id;
    private String name;
    private String description;

    public static Profile create(ProfileEnum profileEnum) {
        Profile profile = new Profile();
        profile.id = UUID.randomUUID();
        profile.name = profileEnum.name();
        profile.description = profile.getDescription();
        return profile;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
