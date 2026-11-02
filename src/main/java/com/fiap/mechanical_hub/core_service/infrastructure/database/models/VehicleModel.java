package com.fiap.mechanical_hub.core_service.infrastructure.database.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles", indexes = {
		@Index(name = "idx_vehicles_customer_id", columnList = "customer_id"),
		@Index(name = "idx_vehicles_license_plate", columnList = "license_plate")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModel {

	@Id
	@Column(name = "id", columnDefinition = "UUID")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false)
	private CustomerModel customer;

	@Column(name = "license_plate", nullable = false, unique = true, length = 10)
	private String licensePlate;

	@Column(name = "brand", nullable = false, length = 100)
	private String brand;

	@Column(name = "model", nullable = false, length = 200)
	private String model;

	@Column(name = "year", nullable = false)
	private Integer year;

	@Column(name = "color", nullable = false, length = 50)
	private String color;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
