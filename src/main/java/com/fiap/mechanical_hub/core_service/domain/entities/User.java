package com.fiap.mechanical_hub.core_service.domain.entities;

import java.util.UUID;

public class User {

    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private Profile profile;

    public static User create(
            String name,
            String email,
            String passwordHash,
            Profile profile
    ) {
        User user = new User();
        user.id = UUID.randomUUID();
        user.name = name;
        user.email = email;
        user.passwordHash = passwordHash;
        user.profile = profile;

        return user;
    }

    public static User build(
            UUID id,
            String name,
            String email,
            String passwordHash,
            Profile profile
    ) {
        User user = new User();

        user.id = id;
        user.name = name;
        user.email = email;
        user.passwordHash = passwordHash;
        user.profile = profile;

        return user;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Profile getProfile() { return profile; }
}
