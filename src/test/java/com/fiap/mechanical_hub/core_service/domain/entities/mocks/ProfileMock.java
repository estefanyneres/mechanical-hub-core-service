package com.fiap.mechanical_hub.core_service.domain.entities.mocks;

import com.fiap.mechanical_hub.core_service.domain.entities.Profile;
import com.fiap.mechanical_hub.core_service.domain.enums.ProfileEnum;

public class ProfileMock {

    public static Profile defaultProfile() {
        return Profile.create(ProfileEnum.ADMINISTRATOR);
    }

    public static Profile profileWithCustomEnum(ProfileEnum profileEnum) {
        return Profile.create(profileEnum);
    }
}
