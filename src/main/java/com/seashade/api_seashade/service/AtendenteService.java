package com.seashade.api_seashade.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.seashade.api_seashade.model.Atendente;
import com.seashade.api_seashade.model.Quiosque;
import com.seashade.api_seashade.repository.AtendenteRepository;
import com.seashade.api_seashade.repository.QuiosqueRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AtendenteService {

    private final AtendenteRepository atendenteRepository;
    private final QuiosqueRepository quiosqueRepository;
    private final EmailService emailService; 

    public AtendenteService(AtendenteRepository atendenteRepository, 
                            QuiosqueRepository quiosqueRepository, 
                            EmailService emailService) {
        this.atendenteRepository = atendenteRepository;
        this.quiosqueRepository = quiosqueRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Atendente criarAtendente(Long quiosqueId, String nome, String email) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));

        Optional<Atendente> existingByEmail = atendenteRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado para outro atendente.");
        }

        Atendente novoAtendente = new Atendente();
        novoAtendente.setNome(nome);
        novoAtendente.setEmail(email); 
        novoAtendente.setQuiosque(quiosque);

        String codigo;
        do {
            codigo = String.format("%04d", new SecureRandom().nextInt(10000));
        } while (atendenteRepository.findByCodigo(codigo).isPresent()); 

        novoAtendente.setCodigo(codigo);

        atendenteRepository.save(novoAtendente);

        emailService.sendAccessCodeEmail(novoAtendente.getEmail(), novoAtendente.getNome(), novoAtendente.getCodigo());

        return novoAtendente;
    }

    public List<Atendente> listarAtendentesPorQuiosque(Long quiosqueId) {
        Quiosque quiosque = quiosqueRepository.findById(quiosqueId)
                .orElseThrow(() -> new EntityNotFoundException("Quiosque não encontrado"));
        return atendenteRepository.findByQuiosque(quiosque);
    }
}
