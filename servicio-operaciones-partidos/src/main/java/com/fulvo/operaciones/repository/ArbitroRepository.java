package com.fulvo.operaciones.repository;

import com.fulvo.operaciones.models.Arbitro;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ArbitroRepository extends JpaRepository<Arbitro, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Arbitro a WHERE a.dni = :dni")
    Optional<Arbitro> findByIdWithLock(@Param("dni") String dni);
}
