-- Sem order_tasks (repair-service) e sem has_stock_pending (TASK-015,
-- reserva de estoque virou assincrona/pos-pagamento - RFC-001).
CREATE TABLE service_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE RESTRICT,
    order_status VARCHAR(100) NOT NULL,
    created_by_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    responsible_user_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    request_description VARCHAR(255) NOT NULL,
    budget DECIMAL(19, 2),
    estimated_completion_at TIMESTAMP,
    opened_at TIMESTAMP,
    completed_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_orders_vehicle_id ON service_orders(vehicle_id);
CREATE INDEX idx_service_orders_customer_id ON service_orders(customer_id);
CREATE INDEX idx_service_orders_order_status ON service_orders(order_status);
CREATE INDEX idx_service_orders_created_by_user_id ON service_orders(created_by_user_id);
CREATE INDEX idx_service_orders_responsible_user_id ON service_orders(responsible_user_id);
CREATE INDEX idx_service_orders_order_number ON service_orders(order_number);
