package com.fiap.mechanical_hub.core_service.infrastructure.database.repositories;

import com.fiap.mechanical_hub.core_service.infrastructure.database.models.ServiceOrderModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceOrderJpaRepository extends JpaRepository<ServiceOrderModel, UUID> {

    List<ServiceOrderModel> findAllByOrderByCreatedAtDesc();

    Optional<ServiceOrderModel> findByOrderNumber(String orderNumber);

    @Query(value = "SELECT order_number FROM service_orders WHERE order_number LIKE CONCAT('OS-', :yearMonth, '-%') ORDER BY order_number DESC LIMIT 1", nativeQuery = true)
    Optional<String> findLastOrderNumberByYearMonth(@Param("yearMonth") String yearMonth);

    @Query(value = """
            SELECT * FROM service_orders
            WHERE order_status NOT IN ('FINALIZADO', 'ENTREGUE', 'RECUSADO')
            ORDER BY
                CASE order_status
                    WHEN 'EM_EXECUCAO' THEN 1
                    WHEN 'APROVADO' THEN 2
                    WHEN 'AGUARDANDO_APROVACAO' THEN 3
                    WHEN 'EM_DIAGNOSTICO' THEN 4
                    WHEN 'RECEBIDO' THEN 5
                    ELSE 6
                END ASC,
                created_at ASC
            """, nativeQuery = true)
    List<ServiceOrderModel> findActiveOrdersSortedByPriority();
}
