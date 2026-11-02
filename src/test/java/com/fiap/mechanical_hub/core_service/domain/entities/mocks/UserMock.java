package com.fiap.mechanical_hub.core_service.domain.entities.mocks;

import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.entities.User;

import static com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants.*;

public class UserMock {

    public static User defaultUser() {
        Profile profile = ProfileMock.defaultProfile();
        return User.create(DEFAULT_USER_NAME, DEFAULT_USER_EMAIL, DEFAULT_USER_PASSWORD_HASH, profile);
    }

    public static User userWithCustomValues(String name, String email, String passwordHash, Profile profile) {
        return User.create(name, email, passwordHash, profile);
    }

    public static User buildUser(String name, String email, String passwordHash, Profile profile) {
        return User.build(DEFAULT_USER_ID, name, email, passwordHash, profile);
    }
}
