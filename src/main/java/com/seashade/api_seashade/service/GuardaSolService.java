package com.seashade.api_seashade.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.seashade.api_seashade.model.GuardaSol;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.repository.GuardaSolRepository;
import com.seashade.api_seashade.repository.QuiosqueRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class GuardaSolService {

    private final GuardaSolRepository guardaSolRepository;
    private final QuiosqueRepository quiosqueRepository;

    public GuardaSolService(GuardaSolRepository guardaSolRepository, QuiosqueRepository quiosqueRepository) {
        this.guardaSolRepository = guardaSolRepository;
        this.quiosqueRepository = quiosqueRepository;
    }

    public List<GuardaSol> listarGuardaSoisPorQuiosque(UUID quiosqueId) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));
        return guardaSolRepository.findByQuiosque(quiosque);
    }

    @Transactional
    public GuardaSol criarGuardaSol(UUID quiosqueId, String identificacao) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));

        GuardaSol novoGuardaSol = new GuardaSol();
        novoGuardaSol.setIdentificacao(identificacao);
        novoGuardaSol.setQuiosque(quiosque);
        novoGuardaSol.setStatus(GuardaSol.StatusGuardaSol.LIVRE); // Todo novo guarda-sol começa como LIVRE

        return guardaSolRepository.save(novoGuardaSol);
    }

    @Transactional
    public GuardaSol mudarStatus(Long guardaSolId, GuardaSol.StatusGuardaSol novoStatus) {
        GuardaSol guardaSol = guardaSolRepository.findById(guardaSolId)
                .orElseThrow(() -> new EntityNotFoundException("Guarda-Sol não encontrado"));

        guardaSol.setStatus(novoStatus);

        return guardaSolRepository.save(guardaSol);
    }


}
