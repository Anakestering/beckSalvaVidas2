
package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.PostoRepository;

import jakarta.transaction.Transactional;

@Service
public class CheckService {

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private ArquivoService arquivoService;

    @Autowired
    private CheckinRepository checkinRepository;

    @Transactional
    public CheckinResponseDTO checkin(CheckinDTO dto) {

        Posto posto = postoRepository.findById(dto.getPostoId()).orElseThrow();

        // Valida limite de 3 checkins por dia
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(23, 59, 59);

        List<Checkin> checkinsHoje = checkinRepository
                .findByPostoIdAndDataHoraBetween(posto.getId(), inicio, fim);

        if (checkinsHoje.size() >= 3) {
            throw new RuntimeException("Limite de 3 registros por dia atingido");
        }

        Checkin checkin = new Checkin();
        checkin.setPosto(posto);
        checkin.setDataHora(LocalDateTime.now());

        if (dto.getFoto() != null && !dto.getFoto().isEmpty()) {
            Arquivo arquivo = arquivoService.upload(dto.getFoto());
            checkin.setFoto(arquivo);
        }

        Checkin checkinSalvo = checkinRepository.save(checkin);

        CheckinResponseDTO crd = new CheckinResponseDTO();
        crd.setPosto(posto.getNome());
        crd.setHorario(checkinSalvo.getCreatedAt());
        if (checkinSalvo.getFoto() != null) {
            String nomeArquivo = Paths.get(checkinSalvo.getFoto().getCaminho()).getFileName().toString();
            crd.setFoto("http://localhost:8080/arquivos/" + nomeArquivo);
        }

        return crd;
    }

    public List<CheckinResponseDTO> listarTodos() {
    return checkinRepository.buscarOrdenadosPorPosto()
            .stream()
            .map(this::toResponseDto)
            .toList();
}

    @Transactional
    public void ocultarTodos() {
        List<Checkin> lista = checkinRepository.findAll();

        for (Checkin c : lista) {
            c.setVisivelAdmin(false);
        }

        checkinRepository.saveAll(lista);
    }

    @Transactional
    public void ocultar(Long id) {
        Checkin c = checkinRepository.findById(id).orElseThrow();
        c.setVisivelAdmin(false);
        checkinRepository.save(c);
    }

    public List<CheckinResponseDTO> buscarHoje(Long postoId) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(23, 59, 59);

        return checkinRepository
                .findByPostoIdAndDataHoraBetween(postoId, inicio, fim)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

   



    // Converte dto
    private CheckinResponseDTO toResponseDto(Checkin checkin) {
        CheckinResponseDTO dto = new CheckinResponseDTO();
        dto.setPosto(checkin.getPosto().getNome());
        dto.setHorario(checkin.getDataHora());

        if (checkin.getFoto() != null) {
        String nomeArquivo = Paths.get(checkin.getFoto().getCaminho()).getFileName().toString();
        dto.setFoto("http://localhost:8080/arquivos/" + nomeArquivo);
    }

        return dto;
    }


}