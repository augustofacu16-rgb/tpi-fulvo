package com.fulvo.operaciones.repository;

import com.fulvo.operaciones.models.ActuacionJugador;
import com.fulvo.operaciones.models.TipoActuacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActuacionJugadorRepository extends JpaRepository<ActuacionJugador, Long> {

    List<ActuacionJugador> findAllByTipoAndPartido_TorneoIdAndPartido_Estado(
            TipoActuacion tipo,
            Long torneoId,
            com.fulvo.operaciones.models.EstadoPartido estado
    );
}
