package com.fuel.nexus.repository;

import com.fuel.nexus.entity.Customer;
import com.fuel.nexus.utility.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByMobileNumber(String mobileNumber);

    List<Customer> findByCity(String city);

    List<Customer> findByState(String state);

    List<Customer> findByCustomerType(CustomerType customerType);

    List<Customer> findByActive(boolean active);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);
}

