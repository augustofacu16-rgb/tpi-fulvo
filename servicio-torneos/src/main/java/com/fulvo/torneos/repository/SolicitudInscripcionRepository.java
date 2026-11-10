package com.fulvo.torneos.repository;

import com.fulvo.torneos.domain.EstadoSolicitudInscripcion;
import com.fulvo.torneos.domain.SolicitudInscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudInscripcionRepository extends JpaRepository<SolicitudInscripcion, Long> {

    List<SolicitudInscripcion> findByTorneoId(Long torneoId);

    Optional<SolicitudInscripcion> findByTorneoIdAndFanaticoUsuarioIdAndEstadoNot(
            Long torneoId,
            String fanaticoUsuarioId,
            EstadoSolicitudInscripcion estado
    );

    boolean existsByTorneoIdAndFanaticoUsuarioIdAndEstadoIn(
            Long torneoId,
            String fanaticoUsuarioId,
            List<EstadoSolicitudInscripcion> estados
    );
}
