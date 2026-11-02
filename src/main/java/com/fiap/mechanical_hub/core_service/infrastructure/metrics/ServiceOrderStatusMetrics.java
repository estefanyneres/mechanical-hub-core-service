package com.fiap.mechanical_hub.core_service.infrastructure.metrics;

import com.fiap.mechanical_hub.core_service.domain.enums.OrderStatusEnum;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Duration;

public final class ServiceOrderStatusMetrics {

    private static final String METRIC_NAME = "service_order.status.duration";

    private ServiceOrderStatusMetrics() {}

    public static void recordDuration(
            MeterRegistry registry, OrderStatusEnum fromStatus, OrderStatusEnum toStatus, Duration duration) {
        Timer.builder(METRIC_NAME)
                .tag("from_status", fromStatus.name())
                .tag("to_status", toStatus.name())
                .publishPercentileHistogram()
                .register(registry)
                .record(duration);
    }
}
