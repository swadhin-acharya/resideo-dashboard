package com.openqa.dashboard.repository;

import com.openqa.dashboard.model.entity.Thermostat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ThermostatRepository extends JpaRepository<Thermostat, UUID> {

    List<Thermostat> findByStatus(String status);

    long countByStatus(String status);
}
