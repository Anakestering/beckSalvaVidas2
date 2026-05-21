package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UsuarioDTO dtoBase;

    @BeforeEach
    void setup() {
        // limpa o banco antes de cada teste pra evitar conflito de CPF/email
        usuarioRepository.deleteAll();

        dtoBase = new UsuarioDTO();
        dtoBase.setNome("Teste");
        dtoBase.setCpf("888.888.888-88");
        dtoBase.setEmail(null);
        dtoBase.setTelefone(null);
        dtoBase.setNivelAcesso("PADRAO");
    }

    /* ====================== CREATE ====================== */

    @Test
    @DisplayName("Deve criar usuário sem email com sucesso")
    void deveCriarUsuarioSemEmail() {
        UsuarioDTO resultado = usuarioService.create(dtoBase);

        assertNotNull(resultado);
        assertNotNull(resultado.getId());
        assertNull(resultado.getEmail());
        assertEquals("Teste", resultado.getNome());
    }

    @Test
    @DisplayName("Deve criar usuário com email com sucesso")
    void deveCriarUsuarioComEmail() {
        dtoBase.setEmail("teste@email.com");

        UsuarioDTO resultado = usuarioService.create(dtoBase);

        assertNotNull(resultado);
        assertEquals("teste@email.com", resultado.getEmail());
    }

    @Test
    @DisplayName("Email em branco deve ser tratado como null")
    void deveTratarEmailBrancoComoNull() {
        dtoBase.setEmail("   ");  

        UsuarioDTO resultado = usuarioService.create(dtoBase);

        assertNull(resultado.getEmail());
    }

    @Test
    @DisplayName("CPF duplicado deve lançar erro 400")
    void deveLancarErroCpfDuplicado() {
        usuarioService.create(dtoBase);  

        UsuarioDTO dto2 = new UsuarioDTO();
        dto2.setNome("Outro");
        dto2.setCpf("888.888.888-88");  
        dto2.setEmail(null);
        dto2.setNivelAcesso("PADRAO");

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> usuarioService.create(dto2)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("CPF"));
    }

    @Test
    @DisplayName("Email duplicado deve lançar erro 400")
    void deveLancarErroEmailDuplicado() {
        dtoBase.setEmail("duplicado@email.com");
        usuarioService.create(dtoBase);  // cria o primeiro

        UsuarioDTO dto2 = new UsuarioDTO();
        dto2.setNome("Outro");
        dto2.setCpf("777.777.777-77");  // CPF diferente
        dto2.setEmail("duplicado@email.com");  // mesmo email
        dto2.setNivelAcesso("PADRAO");

        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> usuarioService.create(dto2)
        );

        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Email"));
    }

    /* ====================== UPDATE ====================== */

    @Test
    @DisplayName("Deve atualizar nome do usuário")
    void deveAtualizarUsuario() {
        UsuarioDTO criado = usuarioService.create(dtoBase);

        UsuarioDTO dtoUpdate = new UsuarioDTO();
        dtoUpdate.setNome("Nome Atualizado");
        dtoUpdate.setCpf("888.888.888-88");
        dtoUpdate.setEmail(null);
        dtoUpdate.setTelefone(null);
        dtoUpdate.setNivelAcesso("PADRAO");

        UsuarioDTO resultado = usuarioService.update(criado.getId(), dtoUpdate);

        assertEquals("Nome Atualizado", resultado.getNome());
    }

    @Test
    @DisplayName("Update com ID inexistente deve lançar erro 404")
    void deveLancarErroAoAtualizarIdInexistente() {
        ResponseStatusException ex = assertThrows(
            ResponseStatusException.class,
            () -> usuarioService.update(99999L, dtoBase)
        );

        assertEquals(404, ex.getStatusCode().value());
    }
}