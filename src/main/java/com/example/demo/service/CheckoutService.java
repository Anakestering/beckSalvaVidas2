package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.PostoRepository;

import jakarta.transaction.Transactional;

@Service
public class CheckoutService {

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private RelatorioService relatorioService;

    @Transactional
    public CheckoutResponseDTO checkout(CheckoutDTO dto) {

        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(23, 59, 59);

        boolean temCheckin = checkinRepository
                .existsByPostoIdAndDataHoraBetween(posto.getId(), inicio, fim);

        if (!temCheckin) {
            throw new RuntimeException("É necessário realizar o checkin antes do checkout.");
        }

        if (!relatorioService.existeHoje(posto.getId())) {
            throw new RuntimeException("É necessário enviar o relatório antes do checkout.");
        }

        List<Checkout> checkoutsHoje = checkoutRepository
                .findByPostoIdAndDataHoraBetween(posto.getId(), inicio, fim);

        if (checkoutsHoje.size() >= 3) {
            throw new RuntimeException("Limite de 3 registros por dia atingido.");
        }

        Checkout checkout = new Checkout();
        checkout.setPosto(posto);
        checkout.setDataHora(LocalDateTime.now());

        if (dto.getFoto() != null && !dto.getFoto().isEmpty()) {
            Arquivo arquivo = arquivoService.upload(dto.getFoto());
            checkout.setFoto(arquivo);
        }

        Checkout salvo = checkoutRepository.save(checkout);

        CheckoutResponseDTO response = new CheckoutResponseDTO();
        response.setPosto(posto.getNome());
        response.setId(salvo.getId()); 
        response.setHorario(salvo.getDataHora());

       if (salvo.getFoto() != null) {
    String nomeArquivo = Paths.get(salvo.getFoto().getCaminho()).getFileName().toString();
    response.setFoto("http://localhost:8080/arquivos/" + nomeArquivo);
}
        return response;
    }

    public List<CheckoutResponseDTO> listarTodos() {
        return checkoutRepository.buscarOrdenadosPorPosto()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public void ocultarTodos() {
        List<Checkout> lista = checkoutRepository.findAll();

        for (Checkout c : lista) {
            c.setVisivelAdmin(false);
        }

        checkoutRepository.saveAll(lista);
    }

    @Transactional
    public void ocultar(Long id) {
        Checkout c = checkoutRepository.findById(id).orElseThrow();
        c.setVisivelAdmin(false);
        checkoutRepository.save(c);
    }

    public List<CheckoutResponseDTO> buscarHoje(Long postoId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(23, 59, 59);

        return checkoutRepository
                .findByPostoIdAndDataHoraBetween(postoId, inicio, fim)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    private CheckoutResponseDTO toResponseDto(Checkout checkout) {
        CheckoutResponseDTO dto = new CheckoutResponseDTO();

        dto.setId(checkout.getId()); 
        dto.setPosto(checkout.getPosto().getNome());
        dto.setHorario(checkout.getDataHora());

        if (checkout.getFoto() != null) {
        String nomeArquivo = Paths.get(checkout.getFoto().getCaminho()).getFileName().toString();
        dto.setFoto("http://localhost:8080/arquivos/" + nomeArquivo);
    }

        return dto;
    }
}