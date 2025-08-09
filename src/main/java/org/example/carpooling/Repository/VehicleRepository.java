package org.example.carpooling.Repository;

import org.example.carpooling.Entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle>  findByDriverId(Long id);
}
