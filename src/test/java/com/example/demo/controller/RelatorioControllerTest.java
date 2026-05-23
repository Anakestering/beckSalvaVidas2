package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.RelatorioDTO;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Relatorio;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.RelatorioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class RelatorioControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwt;
    private String token;

    @Autowired
    private PostoRepository postoRepository;
    private Posto posto;

    @Autowired
    private RelatorioRepository relatorioRepository;
    private Relatorio relatorio;

    @BeforeEach
    public void setup() {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();

        this.token = jwt.generateToken(
                "teste@teste.com", NivelAcesso.ADMIN.toString());

        this.posto = new Posto();
        posto.setNome("PostoTest");
        posto.setDescricao("Posto teste");

        this.posto = postoRepository.save(posto);

        this.relatorio = new Relatorio();
        relatorio.setPosto(posto);
        relatorio.setData(LocalDate.now());
        relatorio.setAtaquesManha(20);
        relatorio.setPrevencoesManha(10);
        relatorio.setAtaquesTarde(20);
        relatorio.setPrevencoesTarde(10);
        relatorio.setObservacoes("teste");

        this.relatorio = relatorioRepository.save(relatorio);
    }

    @Test
    @DisplayName("Salvando um relatorio novo")
    void salvarRelatorioNovo() throws Exception {
        RelatorioDTO relatorioDTO = new RelatorioDTO();

        relatorioDTO.setPostoId(posto.getId());
        relatorioDTO.setAtaquesManha(20);
        relatorioDTO.setPrevencoesManha(10);
        relatorioDTO.setAtaquesTarde(20);
        relatorioDTO.setPrevencoesTarde(10);
        relatorioDTO.setObservacoes("teste");

        String json = objectMapper.writeValueAsString(relatorioDTO);

        mockMvc.perform(post("/relatorio")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postoId").value(posto.getId()));
    }

    @Test
    @DisplayName("deve retornar lista de relatorio response")
    void listarRelatorios() throws Exception {

        mockMvc.perform(get("/relatorio")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ================================ OCULTAR TODOS
    // ===================================
    @Test
    @DisplayName("ocultar todos relatorios como admin")
    void ocultarTodosComoAdmin() throws Exception {

        mockMvc.perform(patch("/relatorio/ocultar-todos")
                .header("Authorization", "Bearer " + token)
                .header("NivelAcesso", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ocultar todos sem permissao")
    void ocultarTodosSemPermissao() throws Exception {

        String tokenPadrao = jwt.generateToken("teste@teste.com", NivelAcesso.PADRAO.toString());

        mockMvc.perform(patch("/relatorio/ocultar-todos")
                .header("Authorization", "Bearer " + tokenPadrao)
                .header("NivelAcesso", "PADRAO"))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("tentar ocultar sem token")
    void ocultarTodosSemToken() throws Exception {

        mockMvc.perform(patch("/relatorio/ocultar-todos"))
                .andExpect(status().isUnauthorized());
    }

    // ============================= OCULTA POR ID ==========================

    @Test
    @DisplayName("ocultar por id como admin")
    void ocultarPorIdComoAdmin() throws Exception {

        mockMvc.perform(patch("/relatorio/ocultar/" + relatorio.getId())
                .header("Authorization", "Bearer " + token)
                .header("NivelAcesso", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ocultar por id sem permissao")
    void ocultarPorIdSemPermissao() throws Exception {

        String tokenPadrao = jwt.generateToken("teste@teste.com", NivelAcesso.PADRAO.toString());

        mockMvc.perform(patch("/relatorio/ocultar/" + relatorio.getId())
                .header("Authorization", "Bearer " + tokenPadrao)
                .header("NivelAcesso", "PADRAO"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("tentar ocultar sem token por id")
    void ocultarPorIdSemToken() throws Exception {

        mockMvc.perform(patch("/relatorio/ocultar/" + relatorio.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ============= buscaHoje ==============
    @Test
    @DisplayName("Busca relatorio de um posto expecifico")
    void buscaRelatorioHojePostoId() throws Exception {

        mockMvc.perform(get("/relatorio/hoje/" + posto.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postoId").value(posto.getId()));
    }

    // =========== lista todos os relatorios de um posto ========
    @Test
    @DisplayName("Busca todos relatorios de um posto expecifico")
    void buscaTodosRelatorioPostoId() throws Exception {

        mockMvc.perform(get("/relatorio/posto/" + posto.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].postoId").value(posto.getId()));
    }

    // =============== exportar relatorios ==============
    @Test
    @DisplayName("exportar com sucesso")
    void exportarRelatoriosSucesso() throws Exception{

        mockMvc.perform(get("/relatorio/exportar")
            .header("Authorization", "Bearer " + token)
            .param("inicio", "2025-01-01")
            .param("fim", "2025-12-31"))
            .andExpect(status().isOk())
            //verifica se realmente é um excel
            .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            
    }

}
