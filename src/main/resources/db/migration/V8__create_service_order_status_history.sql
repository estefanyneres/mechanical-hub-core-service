CREATE TABLE service_order_status_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_order_id UUID NOT NULL REFERENCES service_orders(id) ON DELETE CASCADE,
    from_status VARCHAR(100) NOT NULL,
    to_status VARCHAR(100) NOT NULL,
    changed_by_user_id UUID REFERENCES users(id) ON DELETE RESTRICT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_service_order_status_history_service_order_id ON service_order_status_history(service_order_id);
CREATE INDEX idx_service_order_status_history_changed_at ON service_order_status_history(changed_at);
