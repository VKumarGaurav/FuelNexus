package com.fuel.nexus.repository;

import com.fuel.nexus.entity.CustomerKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerKycRepository extends JpaRepository<CustomerKyc, Long> {

    Optional<CustomerKyc> findByAadharNumber(String aadharNumber);

    Optional<CustomerKyc> findByPanNumber(String panNumber);

    Optional<CustomerKyc> findByCustomer_Id(Long customerId);

    boolean existsByAadharNumber(String aadharNumber);

    boolean existsByPanNumber(String panNumber);
}
