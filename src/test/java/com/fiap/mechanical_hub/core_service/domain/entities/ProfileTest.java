package com.fiap.mechanical_hub.core_service.domain.entities;

import com.fiap.mechanical_hub.core_service.domain.entities.mocks.ProfileMock;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileTest {

    @Test
    void shouldCreateProfileWithValidEnum() {
        Profile profile = ProfileMock.defaultProfile();

        assertNotNull(profile.getId());
        assertEquals(ProfileEnum.ADMINISTRATOR.name(), profile.getName());
    }

    @Test
    void shouldCreateProfileWithDifferentEnums() {
        Profile adminProfile = ProfileMock.profileWithCustomEnum(ProfileEnum.ADMINISTRATOR);
        Profile mechanicProfile = ProfileMock.profileWithCustomEnum(ProfileEnum.MECHANICAL);

        assertEquals(ProfileEnum.ADMINISTRATOR.name(), adminProfile.getName());
        assertEquals(ProfileEnum.MECHANICAL.name(), mechanicProfile.getName());
    }

    @Test
    void shouldHaveDifferentIdsForDifferentProfiles() {
        Profile profile1 = ProfileMock.defaultProfile();
        Profile profile2 = ProfileMock.defaultProfile();

        assertNotEquals(profile1.getId(), profile2.getId());
    }
}
