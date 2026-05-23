package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.dto.AuthDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AuthControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

     @Autowired
    private WebApplicationContext context;
    private ObjectMapper objectMapper;


    @Autowired
    private UsuarioRepository usuarioRepository;
    private Usuario usuario;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        this.objectMapper = new ObjectMapper();
        this.usuario = new Usuario();
        
        usuario.setNome("teste1");
        usuario.setCpf("12345678910");
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuario.setNivelAcesso(NivelAcesso.ADMIN);

        this.usuario = usuarioRepository.save(usuario);
    
    }

    @Test
    @DisplayName("fazer login certinho")
    void fazerLoginCerto() throws Exception {
        AuthDTO authDTO = new AuthDTO();

        authDTO.setCpf("12345678910");
        authDTO.setSenha("123456");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists())
        .andExpect(jsonPath("$.tipo").value("ADMIN"));
    }

    @Test
    @DisplayName("tentar fazer login com senha errada")
    void loginSenhaErrada() throws Exception {
        AuthDTO authDTO = new AuthDTO();

        authDTO.setCpf("12345678910");
        authDTO.setSenha("qualquercoisa");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Tentar fazer login sem cpf")
    void loginSemCpf() throws Exception{
        AuthDTO authDTO = new AuthDTO();

        authDTO.setCpf("");
        authDTO.setSenha("123456");

        String json = objectMapper.writeValueAsString(authDTO);

        mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isBadRequest());
       }
}
