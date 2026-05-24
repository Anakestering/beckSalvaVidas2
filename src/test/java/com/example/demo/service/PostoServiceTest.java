package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.dto.PostoStatusDTO;
import com.example.demo.dto.ResumoResponseDTO;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Posto;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.PostoRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class PostoServiceTest {

    @Autowired
    private PostoService postoService;

    @Autowired
    private PostoRepository postoRepository;

    @Autowired
    private CheckinRepository checkinRepository;

    private Posto posto;

    @BeforeEach
    void setup() {
        this.posto = new Posto();
        posto.setNome("PostoTeste");
        posto.setDescricao("Teste");
        this.posto = postoRepository.save(posto);
    }

    @Test
    @DisplayName("status sem checkin")
    void statusSemCheckin() {
        List<PostoStatusDTO> resultado = postoService.buscarStatusPostos();

        PostoStatusDTO status = resultado.stream()
                .filter(s -> s.getPostoId().equals(posto.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(0, status.getCheckins());
        assertFalse(status.isAtrasado());
    }

    @Test
    @DisplayName("teste de status atrasado")
    void statusAtrasado() throws Exception {

        Checkin checkin = new Checkin();
        checkin.setPosto(posto);
        checkin.setDataHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 30)));
        checkinRepository.save(checkin);

        List<PostoStatusDTO> resultado = postoService.buscarStatusPostos();

        PostoStatusDTO status = resultado.stream()
                .filter(s -> s.getPostoId().equals(posto.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, status.getCheckins());
        assertTrue(status.isAtrasado());
    }

    @Test
    @DisplayName("teste de status no prazo")
    void statusNoPrazo() throws Exception {

        Checkin checkin = new Checkin();
        checkin.setPosto(posto);
        checkin.setDataHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 00)));
        checkinRepository.save(checkin);

        List<PostoStatusDTO> resultado = postoService.buscarStatusPostos();

        PostoStatusDTO status = resultado.stream()
                .filter(s -> s.getPostoId().equals(posto.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, status.getCheckins());
        assertFalse(status.isAtrasado());

    }

    @Test
    @DisplayName("testando resumo-hoje")
    void resumoHoje() {

        //chama o metodo no service
        ResumoResponseDTO resultado = postoService.resumoPosto(posto.getId());

        //confere se existem
        assertNotNull(resultado);
        assertNotNull(resultado.getCheckins());
        assertNotNull(resultado.getCheckouts());
        assertNull(resultado.getRelatorio());

    }
}
