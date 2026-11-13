package com.fulvo.operaciones.service;

import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.operaciones.dto.CrearArbitroRequest;
import com.fulvo.operaciones.models.Arbitro;
import com.fulvo.operaciones.repository.ArbitroRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArbitroService {

    private final ArbitroRepository arbitroRepository;
    private final EntityManager entityManager;

    @Transactional
    public Arbitro crear(CrearArbitroRequest request) {
        if (arbitroRepository.existsById(request.getDni())) {
            throw new ConflictoEstadoException(
                    "Ya existe un árbitro con DNI " + request.getDni()
            );
        }

        Arbitro arbitro = new Arbitro(
                request.getDni(),
                request.getUsuarioId(),
                request.getNombre(),
                request.getApellido()
        );

        try {
            entityManager.persist(arbitro);
            entityManager.flush();
            return arbitro;
        } catch (Exception ex) {
            if (isDuplicateKeyException(ex)) {
                log.warn("Conflicto de clave duplicada al registrar árbitro con DNI {}: {}",
                        request.getDni(), ex.getMessage());
                throw new ConflictoEstadoException(
                        "Ya existe un árbitro con DNI " + request.getDni(),
                        ex
                );
            }
            throw ex;
        }
    }

    private boolean isDuplicateKeyException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlEx) {
                if ("23505".equals(sqlEx.getSQLState())) {
                    return true;
                }
            }
            if (current instanceof ConstraintViolationException cve) {
                if ("23505".equals(cve.getSQLState())) {
                    return true;
                }
                String constraintName = cve.getConstraintName();
                if (constraintName != null && (constraintName.contains("arbitro_pkey") || constraintName.toLowerCase().contains("arbitro"))) {
                    return true;
                }
            }
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("duplicate key") || lower.contains("unique constraint") || lower.contains("violates unique")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
