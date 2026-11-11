package com.fulvo.torneos.repository;

import com.fulvo.torneos.domain.Equipo;
import com.fulvo.torneos.domain.EstadoEquipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByTorneoId(Long torneoId);

    long countByTorneoIdAndEstado(Long torneoId, EstadoEquipo estado);

    Optional<Equipo> findByTorneoIdAndNombre(Long torneoId, String nombre);

    boolean existsByTorneoIdAndNombreIgnoreCase(Long torneoId, String nombre);
}
