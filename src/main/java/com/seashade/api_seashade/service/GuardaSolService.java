package com.seashade.api_seashade.service;

import java.util.List;

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

    public List<GuardaSol> listarGuardaSoisPorQuiosque(Long quiosqueId) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));
        return guardaSolRepository.findByQuiosqueAndAtivoTrue(quiosque); 
    }

    @Transactional
    public GuardaSol criarGuardaSol(Long quiosqueId, String identificacao) {
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

    @Transactional
    public GuardaSol atualizarIdentificacao(Long guardaSolId, String novaIdentificacao) {
        GuardaSol guardaSol = guardaSolRepository.findById(guardaSolId)
            .orElseThrow(() -> new EntityNotFoundException("Guarda-Sol não encontrado"));

    // Opcional: Verificar se a nova identificação já existe no mesmo quiosque
    // List<GuardaSol> existentes = guardaSolRepository.findByQuiosqueAndIdentificacaoAndAtivoTrue(guardaSol.getQuiosque(), novaIdentificacao);
    // if (!existentes.isEmpty() && !existentes.get(0).getId().equals(guardaSolId)) {
    //     throw new IllegalArgumentException("Identificação '" + novaIdentificacao + "' já está em uso.");
    // }

        guardaSol.setIdentificacao(novaIdentificacao);
        return guardaSolRepository.save(guardaSol);
    }

    // Método para DESATIVAR um guarda-sol (Soft Delete)
    @Transactional
    public void desativarGuardaSol(Long guardaSolId) {
        GuardaSol guardaSol = guardaSolRepository.findById(guardaSolId)
                .orElseThrow(() -> new EntityNotFoundException("Guarda-Sol não encontrado"));

        // Adicionar verificação: Não desativar se estiver OCUPADO?
        if (guardaSol.getStatus() == GuardaSol.StatusGuardaSol.OCUPADO) {
            throw new IllegalStateException("Não é possível desativar um guarda-sol que está ocupado.");
        }

        guardaSol.setAtivo(false);
        guardaSolRepository.save(guardaSol);
    }


}
