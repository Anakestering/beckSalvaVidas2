package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PostoDTO;
import com.example.demo.dto.PostoStatusDTO;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.PostoRepository;

@Service
public class PostoService extends BaseService<Posto, PostoDTO> {

    private final PostoRepository postoRepository;

    public PostoService(PostoRepository repository) {
        super(repository);
        this.postoRepository = repository;
    }

    @Autowired
    private CheckinRepository checkinRepository;
    @Autowired
    private CheckoutRepository checkoutRepository;

    public List<PostoStatusDTO> buscarStatusPostos() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(23, 59, 59);

        List<Posto> postos = postoRepository.findByDeletedAtIsNullOrderByAtivoDescNomeAsc()
                .stream()
                .filter(Posto::isAtivo)
                .toList();

        return postos.stream().map(posto -> {
            List<Checkin> checkins = checkinRepository
                    .findByPostoIdAndDataHoraBetween(posto.getId(), inicio, fim);

            List<Checkout> checkouts = checkoutRepository
                    .findByPostoIdAndDataHoraBetween(posto.getId(), inicio, fim);

            boolean atrasado = checkins.stream().anyMatch(c -> {
                LocalDateTime hora = c.getDataHora();
                return hora.getHour() > 7 || (hora.getHour() == 7 && hora.getMinute() > 30);
            });
            return new PostoStatusDTO(posto.getId(), checkins.size(), checkouts.size(), atrasado);
        }).toList();
    }

    @Override
    public PostoDTO create(PostoDTO dto) {
        Posto posto = toEntity(dto);
        posto.setAtivo(true);
        Posto salvo = postoRepository.save(posto);
        return toDto(salvo);
    }

    public List<PostoDTO> listarOrdenados() {
        return postoRepository.findByDeletedAtIsNullOrderByAtivoDescNomeAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public PostoDTO toDto(Posto entity) {
        PostoDTO dto = super.toDto(entity);
        dto.setAtivo(entity.isAtivo());
        return dto;
    }

    @Override
    public Posto toEntity(PostoDTO dto) {
        try {
            Posto posto = new Posto();
            BeanUtils.copyProperties(dto, posto, "ativo"); // ignora o campo ativo
            return posto;
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao converter PostoDTO para Posto");
        }
    }

    public PostoDTO alternarAtivo(Long id) {
        Posto posto = postoRepository.findById(id).orElseThrow();
        posto.setAtivo(!posto.isAtivo());
        return toDto(postoRepository.save(posto));
    }
}
/*
 * CONTROLLER chama alternarAtivo(5)
 * ↓
 * SERVICE busca o posto de id 5 no banco
 * ↓
 * SERVICE inverte o ativo (true → false)
 * ↓
 * SERVICE salva no banco
 * ↓
 * SERVICE converte pra DTO e devolve
 * ↓
 * CONTROLLER devolve pro front-end
 */