package com.example.customerservice.repository;

import com.example.customerservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {
    Optional<Customer> findByAuthProviderId(String authProviderId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
}
