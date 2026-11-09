package com.fulvo.operaciones.repository;

import com.fulvo.operaciones.models.Partido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartidoRepository extends JpaRepository<Partido, Long> {
}
