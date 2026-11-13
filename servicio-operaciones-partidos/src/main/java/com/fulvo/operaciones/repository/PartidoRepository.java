package com.fulvo.operaciones.repository;

import com.fulvo.operaciones.models.Partido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PartidoRepository extends JpaRepository<Partido, Long> {

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
