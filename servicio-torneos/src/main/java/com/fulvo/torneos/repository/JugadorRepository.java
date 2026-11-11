package com.fulvo.torneos.repository;

import com.fulvo.torneos.domain.Jugador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JugadorRepository extends JpaRepository<Jugador, Long> {

    List<Jugador> findByEquipoId(Long equipoId);

    @Query("SELECT j FROM Jugador j WHERE j.equipo.torneo.id = :torneoId AND j.dni = :dni")
    Optional<Jugador> findByTorneoIdAndDni(@Param("torneoId") Long torneoId, @Param("dni") String dni);

    @Query("SELECT COUNT(j) > 0 FROM Jugador j WHERE j.equipo.torneo.id = :torneoId AND j.dni IN :dnis")
    boolean existsByTorneoIdAndDniIn(@Param("torneoId") Long torneoId, @Param("dnis") List<String> dnis);
}
