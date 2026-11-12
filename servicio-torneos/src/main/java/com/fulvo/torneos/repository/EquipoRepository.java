package com.fulvo.torneos.repository;

import com.fulvo.torneos.domain.Equipo;
import com.fulvo.torneos.domain.EstadoEquipo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Long> {

    List<Equipo> findByTorneoId(Long torneoId);

    List<Equipo> findByTorneoIdAndEstadoOrderByIdAsc(Long torneoId, EstadoEquipo estado);

    long countByTorneoIdAndEstado(Long torneoId, EstadoEquipo estado);

    Optional<Equipo> findByTorneoIdAndNombre(Long torneoId, String nombre);

    boolean existsByTorneoIdAndNombreIgnoreCase(Long torneoId, String nombre);

    /**
     * Bloqueo pesimista (SELECT ... FOR UPDATE) sobre la fila del equipo (Subsección 3.4).
     * Serializa reemplazos y bajas concurrentes para que el contador de cambios no se corrompa.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Equipo e WHERE e.id = :id")
    Optional<Equipo> findByIdWithLock(@Param("id") Long id);
}
