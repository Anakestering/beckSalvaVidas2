package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.entity.Arquivo;
import com.example.demo.service.ArquivoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ArquivoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    private ObjectMapper objectMapper;

    @Autowired
    private ArquivoService arquivoService;
    private Arquivo arquivo;

    @BeforeEach
    void setup() throws Exception {

        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();
        MockMultipartFile fotoMock = new MockMultipartFile(
                "foto",
                "bombeiro.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "conteudo".getBytes());

        this.arquivo = arquivoService.upload(fotoMock);
    }

    @Test
    @DisplayName("encontrar arquivo")
    void encontrarArquivo() throws Exception {

        String nomeNodisco = Paths.get(arquivo.getCaminho()).getFileName().toString();

        mockMvc.perform(get("/arquivos/" + nomeNodisco))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("n encontrar arquivo")
    void naoEcontrarArquivo() throws Exception {

        mockMvc.perform(get("/arquivos/nomealeatorio"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
