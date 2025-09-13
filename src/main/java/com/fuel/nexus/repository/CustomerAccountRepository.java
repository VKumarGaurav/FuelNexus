package com.fuel.nexus.repository;

import com.fuel.nexus.entity.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, Long> {

    Optional<CustomerAccount> findByUsername(String username);

    Optional<CustomerAccount> findByCustomer_Id(Long customerId);

    boolean existsByUsername(String username);
}
