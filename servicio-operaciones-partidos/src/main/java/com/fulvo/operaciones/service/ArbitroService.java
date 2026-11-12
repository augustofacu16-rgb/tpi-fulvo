package com.fulvo.operaciones.service;

import com.fulvo.common.exception.ConflictoEstadoException;
import com.fulvo.operaciones.dto.CrearArbitroRequest;
import com.fulvo.operaciones.models.Arbitro;
import com.fulvo.operaciones.repository.ArbitroRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

        // Usamos persists porque save podría dar problemas si ya existe el dni
        entityManager.persist(arbitro);
        return arbitro;
    }
}
