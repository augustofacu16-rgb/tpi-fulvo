package com.fulvo.torneos.repository;

import com.fulvo.torneos.domain.EstadoNovedadPartido;
import com.fulvo.torneos.domain.NovedadCronograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para novedades y trazabilidad del cronograma de partidos (Subsección 4.5).
 */
public interface NovedadCronogramaRepository extends JpaRepository<NovedadCronograma, Long> {

    List<NovedadCronograma> findByTorneoIdOrderByFechaRegistroDesc(Long torneoId);

    List<NovedadCronograma> findByTorneoIdAndEstadoPartidoOrderByFechaRegistroDesc(Long torneoId, EstadoNovedadPartido estadoPartido);

    Optional<NovedadCronograma> findByTorneoIdAndPartidoId(Long torneoId, Long partidoId);

    @Query("""
            SELECT n FROM NovedadCronograma n
            WHERE n.torneoId = :torneoId
              AND (n.equipoLocalId = :equipoId OR n.equipoVisitanteId = :equipoId)
            ORDER BY n.fechaRegistro DESC
            """)
    List<NovedadCronograma> findByTorneoIdAndEquipoId(@Param("torneoId") Long torneoId, @Param("equipoId") Long equipoId);
}
