package com.fiap.mechanical_hub.core_service.application.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.serviceorder.*;
import com.fiap.mechanical_hub.core_service.domain.entities.ServiceOrder;

public interface ServiceOrderPresenter {
    ServiceOrderResponse toResponse(ServiceOrder order);
    ServiceOrderDetailResponse toDetailResponse(ServiceOrderDetailOutput output);
    ServiceOrderSummaryResponse toSummaryResponse(ServiceOrderSummaryOutput output);
    ServiceOrderCustomerView toCustomerView(ServiceOrderCustomerOutput output);
}
