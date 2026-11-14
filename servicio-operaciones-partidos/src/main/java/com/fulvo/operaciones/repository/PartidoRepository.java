package com.fulvo.operaciones.repository;

import com.fulvo.operaciones.models.Partido;
import com.fulvo.operaciones.models.EstadoPartido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

    @Query("""
            select p.id from Partido p
            where p.torneoId = :torneoId and p.formatoTorneo = 'LLAVES'
              and p.fechaOFase = :fase
            order by p.id
            """)
    List<Long> findIdsLlavesPorFaseOrdenados(
            @Param("torneoId") Long torneoId,
            @Param("fase") Integer fase
    );

    boolean existsByTorneoIdAndFormatoTorneoAndFechaOFaseGreaterThan(
            Long torneoId, String formatoTorneo, Integer fase
    );

    List<Partido> findAllByTorneoIdAndEstado(
            Long torneoId,
            EstadoPartido estado
    );

    List<Partido> findAllByTorneoIdAndEstadoIn(
            Long torneoId,
            List<EstadoPartido> estados
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Partido p WHERE p.id = :id")
    Optional<Partido> findByIdWithLock(@Param("id") Long id);

    @Query("""
            select count(p)
            from Partido p
            where p.sede.id = :sedeId
              and p.fechaHora > :desde
              and p.fechaHora < :hasta
              and p.id <> :partidoId
            """)
    long countSedeConflictos(
            @Param("sedeId") Long sedeId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("partidoId") Long partidoId
    );

    @Query("""
            select count(p)
            from Partido p
            where p.arbitro.dni = :arbitroDni
              and p.fechaHora > :desde
              and p.fechaHora < :hasta
              and p.id <> :partidoId
            """)
    long countArbitroConflictos(
            @Param("arbitroDni") String arbitroDni,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta,
            @Param("partidoId") Long partidoId
    );

}
