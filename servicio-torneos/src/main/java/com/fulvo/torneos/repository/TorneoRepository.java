package com.fulvo.torneos.repository;

import com.fulvo.torneos.domain.Torneo;
import com.fulvo.torneos.dto.TorneoResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

@Repository
public interface TorneoRepository extends JpaRepository<Torneo, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Torneo t WHERE t.id = :id")
    Optional<Torneo> findByIdWithLock(@Param("id") Long id);

    @Query("""
        SELECT new com.fulvo.torneos.dto.TorneoResponseDTO(
            t.id,
            t.nombre,
            t.formato,
            t.cupoEquipos,
            COUNT(e.id),
            t.estado,
            t.fechaInicio,
            t.fechaCreacion
        )
        FROM Torneo t
        LEFT JOIN Equipo e ON e.torneo.id = t.id AND e.estado = com.fulvo.torneos.domain.EstadoEquipo.ACTIVO
        GROUP BY t.id, t.nombre, t.formato, t.cupoEquipos, t.estado, t.fechaInicio, t.fechaCreacion
        ORDER BY t.id ASC
    """)
    List<TorneoResponseDTO> findAllConConteoEquipos();
    @Query("""
        SELECT new com.fulvo.torneos.dto.TorneoResponseDTO(
            t.id,
            t.nombre,
            t.formato,
            t.cupoEquipos,
            COUNT(e.id),
            t.estado,
            t.fechaInicio,
            t.fechaCreacion
        )
        FROM Torneo t
        LEFT JOIN Equipo e ON e.torneo.id = t.id AND e.estado = com.fulvo.torneos.domain.EstadoEquipo.ACTIVO
        WHERE t.id = :id
        GROUP BY t.id, t.nombre, t.formato, t.cupoEquipos, t.estado, t.fechaInicio, t.fechaCreacion
    """)
    Optional<TorneoResponseDTO> findTorneoDTOById(@Param("id") Long id);
}
