
package com.example.demo.service;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.dto.RelatorioDTO;
import com.example.demo.dto.RelatorioResponseDTO;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Relatorio;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.RelatorioRepository;

import jakarta.transaction.Transactional;

@Service
public class RelatorioService {

    @Autowired
    private RelatorioRepository relatorioRepository;

    @Autowired
    private PostoRepository postoRepository;

    @Transactional
    public RelatorioResponseDTO salvar(RelatorioDTO dto) {

        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        LocalDate hoje = LocalDate.now();

        // Busca relatório existente do dia para sobrescrever, ou cria novo
        Optional<Relatorio> existente = relatorioRepository
                .findByPostoIdAndData(dto.getPostoId(), hoje);

        Relatorio relatorio = existente.orElseGet(Relatorio::new);

        relatorio.setPosto(posto);
        relatorio.setData(hoje);
        relatorio.setAtaquesManha(dto.getAtaquesManha());
        relatorio.setPrevencoesManha(dto.getPrevencoesManha());
        relatorio.setAtaquesTarde(dto.getAtaquesTarde());
        relatorio.setPrevencoesTarde(dto.getPrevencoesTarde());
        relatorio.setObservacoes(dto.getObservacoes());

        Relatorio salvo = relatorioRepository.save(relatorio);

        return toResponse(salvo);
    }

    public boolean existeHoje(Long postoId) {
        return relatorioRepository.existsByPostoIdAndData(postoId, LocalDate.now());
    }

    private RelatorioResponseDTO toResponse(Relatorio r) {
        RelatorioResponseDTO dto = new RelatorioResponseDTO();
        dto.setPostoId(r.getPosto().getId());
        dto.setPosto(r.getPosto().getNome());
        dto.setData(r.getData());
        dto.setAtaquesManha(r.getAtaquesManha());
        dto.setPrevencoesManha(r.getPrevencoesManha());
        dto.setAtaquesTarde(r.getAtaquesTarde());
        dto.setPrevencoesTarde(r.getPrevencoesTarde());
        dto.setObservacoes(r.getObservacoes());
        return dto;
    }

    public List<RelatorioResponseDTO> listarTodos() {
        return relatorioRepository.findAllByOrderByPosto_IdAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void ocultarTodos() {
        List<Relatorio> lista = relatorioRepository.findAll();

        for (Relatorio r : lista) {
            r.setVisivelAdmin(false);
        }

        relatorioRepository.saveAll(lista);
    }

    @Transactional
    public void ocultar(Long id) {
        Relatorio r = relatorioRepository.findById(id).orElseThrow();
        r.setVisivelAdmin(false);
        relatorioRepository.save(r);
    }

    public RelatorioResponseDTO buscarHoje(Long postoId) {
        return relatorioRepository
                .findByPostoIdAndData(postoId, LocalDate.now())
                .map(this::toDto)
                .orElse(null);
    }

    public List<RelatorioResponseDTO> listarPorPosto(Long postoId) {
        return relatorioRepository.findByPostoId(postoId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Converte dto
    private RelatorioResponseDTO toDto(Relatorio relatorio) {
        RelatorioResponseDTO dto = new RelatorioResponseDTO();
        dto.setPostoId(relatorio.getPosto().getId());
        dto.setPosto(relatorio.getPosto().getNome());
        dto.setData(relatorio.getData());
        dto.setAtaquesManha(relatorio.getAtaquesManha());
        dto.setPrevencoesManha(relatorio.getPrevencoesManha());
        dto.setAtaquesTarde(relatorio.getAtaquesTarde());
        dto.setPrevencoesTarde(relatorio.getPrevencoesTarde());
        dto.setObservacoes(relatorio.getObservacoes());
        return dto;
    }
}