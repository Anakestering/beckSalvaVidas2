package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.PostoDTO;
import com.example.demo.entity.Posto;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.PostoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

//diz que é um teste Spring e sobe toda a aplicação para testar
@SpringBootTest
@ActiveProfiles("test")
public class PostoControllerTest {
    // (aq fica as criações "universais" pra ser usado por todas as funções se
    // quiser)

    // Simula requisições HTTP sem precisar iniciar o servidor
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    // ObjectMapper transforma objetos Java em JSON e vice-versa
    private ObjectMapper objectMapper;

    // pega o token do autenticacaoTest
    @Autowired
    private JwtUtil jwt;
    private String token;

    @Autowired
    private PostoRepository postoRepository;

    // "Antes de cada" Executa este método antes de cada teste
    @BeforeEach
    public void setup() {
        // Cria o MockMvc usando o contexto da aplicação
        // O build():finaliza a construção do objeto.
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        // Inicializa o conversor de objetos para JSON
        this.objectMapper = new ObjectMapper();

        // Gera o token de admin para todos os testes
        this.token = jwt.generateToken(
                "teste@teste.com", NivelAcesso.ADMIN.toString());

    }

    /* ====================== CRUD letra c ====================== */

    @Test
    @DisplayName("Deve criar um posto com sucesso")
    void criarPosto() throws Exception {
        PostoDTO postoDTO = new PostoDTO();

        // seta valores
        postoDTO.setNome("Posto 1");
        postoDTO.setDescricao("Posto 1");

        // Converte o objeto DTO inteiro para JSON
        String json = objectMapper.writeValueAsString(postoDTO);

        // faz a requisição
        mockMvc.perform(post("/postos")
                // define o corpo da requisição (os dados enviado) nesse caso será JSON
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                // define o token q criamos la em cima com o corpo necessario
                .header("Authorization", "Bearer " + token))
                // asserções
                // "e espera-se": verifica se o status da resposta foi 200 OK
                .andExpect(status().isOk())
                // o json nome com conteudo posto 1
                .andExpect(jsonPath("$.nome").value("Posto 1"))
                // Verifica se o campo "id" existe na resposta JSON
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Criar posto sem mandar campos obrigatórios")
    void CriarPostoSemCampoObrigatorio() throws Exception {

        mockMvc.perform(post("/postos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    /* ====================== CRUD letra R ====================== */

    @Test
    @DisplayName("deve listar todos os postos")
    void listarPosto() throws Exception {

        mockMvc.perform(get("/postos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /* ====================== CRUD letra U ====================== */

    @Test
    @DisplayName("Deve buscar posto pelo ID e mudar descrição")
    void buscarPorId() throws Exception {
        Posto posto = new Posto();
        posto.setNome("Posto para buscar por id");
        posto.setDescricao("Posto buscavel");

        posto = postoRepository.save(posto);

        mockMvc.perform(get("/postos/" + posto.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Posto para buscar por id"));

    }

    /* ====================== CRUD letra D ====================== */

    @Test
    @DisplayName("Deve deletar posto pelo id")
    void deletarPosto() throws Exception {
        Posto posto = new Posto();
        posto.setNome("Posto para DELETAR por id");
        posto.setDescricao("Posto buscavel");

        posto = postoRepository.save(posto);

        mockMvc.perform(delete("/postos/" + posto.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        posto = postoRepository.findById(posto.getId()).orElseThrow();

        assertFalse(posto.isAtivo());

    }

}
