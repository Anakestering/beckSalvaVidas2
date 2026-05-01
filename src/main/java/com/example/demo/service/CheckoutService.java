
package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
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

    @Transactional
    public CheckoutResponseDTO checkout(CheckoutDTO dto) {

        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        // Valida limite de 3 checkins por dia
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(23, 59, 59);

        

        List<Checkout> checkoutsHoje = checkoutRepository
                .findByPostoIdAndDataHoraBetween(posto.getId(), inicio, fim);

        if (checkoutsHoje.size() >= 3) {
            throw new RuntimeException("Limite de 3 registros por dia atingido");
        }

        Checkout checkout = new Checkout();
        checkout.setPosto(posto);
        checkout.setDataHora(LocalDateTime.now());

        if (dto.getFoto() != null && !dto.getFoto().isEmpty()) {
            Arquivo arquivo = arquivoService.upload(dto.getFoto());
            checkout.setFoto(arquivo);
        }

        Checkout checkoutSalvo = checkoutRepository.save(checkout);

        CheckoutResponseDTO crd = new CheckoutResponseDTO();
        crd.setPosto(posto.getNome());
        crd.setHorario(checkoutSalvo.getCreatedAt());

        return crd;
    }

    public List<CheckoutDTO> listarTodos() {
        return checkoutRepository.buscarOrdenadosPorPosto()
                .stream()
                .map(this::toDto)
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

    public List<CheckoutDTO> buscarHoje(Long postoId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(23, 59, 59);

        return checkoutRepository
                .findByPostoIdAndDataHoraBetween(postoId, inicio, fim)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // Converte dto
    private CheckoutDTO toDto(Checkout checkout) {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setPostoId(checkout.getPosto().getId());
        dto.setDataHora(checkout.getDataHora());
        return dto;
    }
}