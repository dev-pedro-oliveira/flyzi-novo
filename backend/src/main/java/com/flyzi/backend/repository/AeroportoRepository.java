package com.flyzi.backend.repository;

import com.flyzi.backend.model.Aeroporto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AeroportoRepository extends JpaRepository<Aeroporto, Long> {
    Optional<Aeroporto> findByIata(String iata);
    List<Aeroporto> findByEstado(String estado);
    List<Aeroporto> findAll();
}