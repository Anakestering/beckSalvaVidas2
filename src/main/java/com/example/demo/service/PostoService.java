package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.PostoDTO;
import com.example.demo.entity.Posto;
import com.example.demo.repository.PostoRepository;

@Service
public class PostoService extends BaseService<Posto, PostoDTO> {

    private final PostoRepository postoRepository;

    public PostoService(PostoRepository repository) {
        super(repository);
        this.postoRepository = repository;
    }

    @Override
public PostoDTO create(PostoDTO dto) {
    Posto posto = toEntity(dto);
    System.out.println("ATIVO ANTES: " + posto.isAtivo());
    posto.setAtivo(true);
    System.out.println("ATIVO DEPOIS: " + posto.isAtivo());
    Posto salvo = postoRepository.save(posto);
    System.out.println("ATIVO SALVO: " + salvo.isAtivo());
    return toDto(salvo);
}

    public List<PostoDTO> listarOrdenados() {
        return postoRepository.findByDeletedAtIsNullOrderByAtivoDescNomeAsc()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PostoDTO alternarAtivo(Long id) {
        Posto posto = postoRepository.findById(id).orElseThrow();
        posto.setAtivo(!posto.isAtivo());
        return toDto(postoRepository.save(posto));
    }
}
/*CONTROLLER chama alternarAtivo(5)
      ↓
SERVICE busca o posto de id 5 no banco
      ↓
SERVICE inverte o ativo (true → false)
      ↓
SERVICE salva no banco
      ↓
SERVICE converte pra DTO e devolve
      ↓
CONTROLLER devolve pro front-end*/