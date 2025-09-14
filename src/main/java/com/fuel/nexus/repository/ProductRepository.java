package com.fuel.nexus.repository;

import com.fuel.nexus.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    boolean existsByManufacturerEmail(String manufacturerEmail);

    Optional<Product> findByName(String name);
}
