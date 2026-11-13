package com.fulvo.operaciones.repository;

import com.fulvo.operaciones.models.Sede;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SedeRepository extends JpaRepository<Sede, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sede s WHERE s.id = :id")
    Optional<Sede> findByIdWithLock(@Param("id") Long id);
}
