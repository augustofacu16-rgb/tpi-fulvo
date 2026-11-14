package com.fulvo.operaciones.repository;

import com.fulvo.operaciones.models.TablaPosicion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TablaPosicionRepository extends JpaRepository<TablaPosicion, Long> {

    Optional<TablaPosicion> findByTorneoIdAndEquipoId(Long torneoId, Long equipoId);

    List<TablaPosicion> findAllByTorneoId(Long torneoId);

    List<TablaPosicion> findAllByTorneoIdOrderByPuntosDescDiferenciaGolDescGolesAFavorDesc(
            Long torneoId
    );
}
