package com.example.demo.config;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;


@SpringBootTest
@ActiveProfiles("test")
public class DataInitializerTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void deveCriarUsuariosIniciais() {
        assertFalse(usuarioRepository.findByNivelAcesso(NivelAcesso.ADMIN).isEmpty());
        assertFalse(usuarioRepository.findByNivelAcesso(NivelAcesso.PADRAO).isEmpty());
    }
}
