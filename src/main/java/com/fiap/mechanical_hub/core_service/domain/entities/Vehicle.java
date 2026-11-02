package com.fiap.mechanical_hub.core_service.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.fiap.mechanical_hub.core_service.shared.utils.license_plate.LicensePlateFormatter.normalize;
import static com.fiap.mechanical_hub.core_service.shared.utils.license_plate.LicensePlateValidator.validateLicensePlate;

public class Vehicle {

	private UUID id;
	private UUID customerId;
	private String licensePlate;
	private String brand;
	private String model;
	private Integer year;
	private String color;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public Vehicle() {}

	public Vehicle(UUID id, UUID customerId, String licensePlate, String brand, String model,
				   Integer year, String color, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.id = id;
		this.customerId = customerId;
		this.licensePlate = licensePlate;
		this.brand = brand;
		this.model = model;
		this.year = year;
		this.color = color;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Vehicle create(UUID customerId, String licensePlate, String brand,
								 String model, Integer year, String color) {
		validateLicensePlate(licensePlate);

		Vehicle vehicle = new Vehicle();
		vehicle.id = UUID.randomUUID();
		vehicle.customerId = customerId;
		vehicle.licensePlate = normalize(licensePlate);
		vehicle.brand = brand;
		vehicle.model = model;
		vehicle.year = year;
		vehicle.color = color;
		vehicle.createdAt = LocalDateTime.now();
		vehicle.updatedAt = LocalDateTime.now();

		return vehicle;
	}

	public void update(String licensePlate, String brand, String model, Integer year, String color) {
		validateLicensePlate(licensePlate);

		this.licensePlate = normalize(licensePlate);
		this.brand = brand;
		this.model = model;
		this.year = year;
		this.color = color;
		this.updatedAt = LocalDateTime.now();
	}

	public UUID getId() { return id; }
	public UUID getCustomerId() { return customerId; }
	public String getLicensePlate() { return licensePlate; }
	public String getBrand() { return brand; }
	public String getModel() { return model; }
	public Integer getYear() { return year; }
	public String getColor() { return color; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public LocalDateTime getUpdatedAt() { return updatedAt; }
}
