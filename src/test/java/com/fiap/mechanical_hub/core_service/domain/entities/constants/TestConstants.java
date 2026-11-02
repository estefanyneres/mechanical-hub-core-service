package com.fiap.mechanical_hub.core_service.domain.entities.constants;

import java.math.BigDecimal;
import java.util.UUID;

public class TestConstants {

    public static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final String DEFAULT_USER_NAME = "John Doe";
    public static final String DEFAULT_USER_EMAIL = "john@example.com";
    public static final String DEFAULT_USER_PASSWORD_HASH = "hashedPassword123";

    public static final UUID DEFAULT_CUSTOMER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final String DEFAULT_CUSTOMER_NAME = "José Silva";
    public static final String DEFAULT_CUSTOMER_DOCUMENT = "52998224725";
    public static final String DEFAULT_CUSTOMER_TELEPHONE = "5511987654321";
    public static final String DEFAULT_CUSTOMER_EMAIL = "customer@example.com";
    public static final String DEFAULT_CUSTOMER_ADDRESS = "Rua Principal, 100";

    public static final UUID DEFAULT_VEHICLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final String DEFAULT_VEHICLE_LICENSE_PLATE = "ABC1D23";
    public static final String DEFAULT_VEHICLE_BRAND = "Toyota";
    public static final String DEFAULT_VEHICLE_MODEL = "Corolla";
    public static final Integer DEFAULT_VEHICLE_YEAR = 2020;
    public static final String DEFAULT_VEHICLE_COLOR = "Branco";

    public static final UUID DEFAULT_SERVICE_ORDER_ID = UUID.fromString("00000000-0000-0000-0000-000000000007");
    public static final String DEFAULT_ORDER_NUMBER = "OS-202601-0001";
    public static final String DEFAULT_REQUEST_DESCRIPTION = "Diagnóstico e reparo da transmissão";
    public static final BigDecimal DEFAULT_ORDER_BUDGET = BigDecimal.valueOf(500.00);

    public static final String DEFAULT_PROFILE_NAME = "ADMINISTRATOR";
}
