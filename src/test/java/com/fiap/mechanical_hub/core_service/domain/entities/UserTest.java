package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.entities.constants.TestConstants;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.ProfileMock;
import com.fiap.mechanical_hub.core_service.domain.entities.mocks.UserMock;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        User user = UserMock.defaultUser();

        assertNotNull(user.getId());
        assertEquals(TestConstants.DEFAULT_USER_NAME, user.getName());
        assertEquals(TestConstants.DEFAULT_USER_EMAIL, user.getEmail());
        assertEquals(TestConstants.DEFAULT_USER_PASSWORD_HASH, user.getPasswordHash());
        assertNotNull(user.getProfile());
    }

    @Test
    void shouldCreateUserWithCustomProfile() {
        Profile profile = ProfileMock.profileWithCustomEnum(ProfileEnum.MECHANICAL);
        User user = UserMock.userWithCustomValues("João Mecânico", "joao@mecanica.com", "hashedPassword", profile);

        assertNotNull(user.getId());
        assertEquals(ProfileEnum.MECHANICAL.name(), user.getProfile().getName());
    }

    @Test
    void shouldBuildUserWithSpecificId() {
        Profile profile = ProfileMock.defaultProfile();
        User user = UserMock.buildUser(
                TestConstants.DEFAULT_USER_NAME, TestConstants.DEFAULT_USER_EMAIL,
                TestConstants.DEFAULT_USER_PASSWORD_HASH, profile);

        assertEquals(TestConstants.DEFAULT_USER_ID, user.getId());
        assertEquals(TestConstants.DEFAULT_USER_NAME, user.getName());
    }

    @Test
    void shouldCreateDifferentUsersWithDifferentIds() {
        User user1 = UserMock.defaultUser();
        User user2 = UserMock.defaultUser();

        assertNotEquals(user1.getId(), user2.getId());
    }
}
