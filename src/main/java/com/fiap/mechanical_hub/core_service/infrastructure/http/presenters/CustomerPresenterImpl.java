package com.fiap.mechanical_hub.core_service.infrastructure.http.presenters;

import com.fiap.mechanical_hub.core_service.application.dto.customer.CustomerResponse;
import com.fiap.mechanical_hub.core_service.application.presenters.CustomerPresenter;
import com.fiap.mechanical_hub.core_service.domain.entities.Customer;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.fiap.mechanical_hub.core_service.shared.utils.document.DocumentFormatter.formatDocument;
import static com.fiap.mechanical_hub.core_service.shared.utils.telephone.TelephoneFormatter.formatTelephone;

@Component
public class CustomerPresenterImpl implements CustomerPresenter {

    @Override
    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getDocumentTypeEnum().getValue(),
                formatDocument(customer.getDocumentTypeEnum().getValue(), customer.getDocumentNumber()),
                formatTelephone(customer.getTelephone()),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    @Override
    public List<CustomerResponse> toResponseList(List<Customer> customers) {
        return customers.stream().map(this::toResponse).toList();
    }
}
