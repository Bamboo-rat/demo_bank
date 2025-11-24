package com.example.corebankingservice.repository;

import com.example.corebankingservice.entity.CIF_Master;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CifMasterRepository extends JpaRepository<CIF_Master, String> {
    boolean existsByUsername(String username);
    boolean existsByNationalId(String nationalId);
    Optional<CIF_Master> findByUsername(String username);
    Optional<CIF_Master> findByCifNumber(String cifNumber);
}
