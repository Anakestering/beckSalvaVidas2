package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

//pra rodar os testes
@SpringBootTest
@Transactional
// mostra que esse arquivo é de test
@ActiveProfiles("test")
public class UsuarioControllerTest {

    // chama o mock pra ser usado (mock simula requisicao)
    private MockMvc mockMvc;

    // é o contexto completo da aplicação Spring
    // (controllers, services, filtros de segurança, tudo).
    @Autowired
    private WebApplicationContext context;

    // sei q transforma de objeto java pra json mas n sei mt bem sobre
    private ObjectMapper objectMapper;

    // chamando esses arquivos pra serem usados na autentificação
    @Autowired
    private JwtUtil jwt;
    private String token;

    // chama o Repository pra mexer com o banco e chama o proprio banco
    @Autowired
    private UsuarioRepository usuarioRepository;
    private Usuario usuario;

    @BeforeEach
    public void setup() {
        // pega a variavel mock q criamos la em cima e falamos q nela será
        // colocada requisições q estarão no contexto desse arquivo
        // preparando ela pra ser usada, n entendi pq tem q fazer isso e n só pegar e
        // usar onde for precisar
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        // dentro do método você só inicializa, não injeta.
        this.objectMapper = new ObjectMapper();

        // pega o objeto token chamado antes e designa a criação dele com esse email e
        // esse nivel de acesso
        // com a funcao "generateToken" q existe na sua classe pra gerar um token para
        // esses dados
        this.token = jwt.generateToken("teste@teste.com", NivelAcesso.ADMIN.toString());

        // cria um usuario pra poder fazer as funcões no sistema
        this.usuario = new Usuario();
        usuario.setNome("teste1");
        usuario.setCpf("12345678910");
        usuario.setEmail("tantofaz@gmail.com");
        usuario.setSenha("123456");
        usuario.setNivelAcesso(NivelAcesso.ADMIN);

        // pede para o usuarioRepository salvar na tabela usuario
        this.usuario = usuarioRepository.save(usuario);

    }

    @Test
    @DisplayName("Deve criar um usuário completo e corretamente.")
    void criarUsuarioCompleto() throws Exception {

        // pra criar um usuario precisa de um dto vindo do front
        // entao tem q chamar o dto pra ser preenchido
        UsuarioDTO usuarioDTO = new UsuarioDTO();

        // e designar/preencher os campos desse dto, como se estivesse no front
        // entao vai td q o usuario manda na requisição
        usuarioDTO.setNome("teste2");
        usuarioDTO.setCpf("987.654.321-10");
        usuarioDTO.setEmail("testeUser@gmail.com");
        usuarioDTO.setTelefone("1234567890");
        // dto recebe string
        usuarioDTO.setNivelAcesso("PADRAO");

        // converte o objeto dto para json
        String json = objectMapper.writeValueAsString(usuarioDTO);

        // agr seria salvar, entao simular requisição pra salvar, chamadno o mock e
        // fazer o assert
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("teste2"))
                .andExpect(jsonPath("$.cpf").value("98765432110"))
                .andExpect(jsonPath("$.email").value("testeUser@gmail.com"))
                .andExpect(jsonPath("$.telefone").value("1234567890"))
                .andExpect(jsonPath("$.nivelAcesso").value("PADRAO"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("Deve tentar criar um usuário com cpf ja cadastrado.")
    void criarUsuarioCpfCadastrado() throws Exception {

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setNome("teste2");
        usuarioDTO.setCpf("123.456.789-10");
        usuarioDTO.setEmail("");
        usuarioDTO.setTelefone("");
        usuarioDTO.setNivelAcesso("PADRAO");

        // converte o objeto dto para json
        String json = objectMapper.writeValueAsString(usuarioDTO);

        // agr seria salvar, entao simular requisição pra salvar, chamadno o mock e
        // fazer o assert
        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @DisplayName("Deve tentar criar um usuário com email ja cadastrado.")
    void criarUsuarioEmailCadastrado() throws Exception {

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setNome("teste2");
        usuarioDTO.setCpf("234.567.890-12");
        usuarioDTO.setEmail("tantofaz@gmail.com");
        usuarioDTO.setTelefone("");
        usuarioDTO.setNivelAcesso("PADRAO");

        String json = objectMapper.writeValueAsString(usuarioDTO);

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @DisplayName("Deve tentar criar um usuário com campo obrigatório faltando.")
    void criarUsuarioErrado() throws Exception {

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        // só pra testar simular 100%
        usuarioDTO.setNome("teste2");
        usuarioDTO.setCpf("");
        usuarioDTO.setEmail("testeUser@gmail.com");
        usuarioDTO.setTelefone("1234567890");
        usuarioDTO.setNivelAcesso("PADRAO");

        String json = objectMapper.writeValueAsString(usuarioDTO);

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @DisplayName("testando a edição")
    void edicaoUsuario() throws Exception {
        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setNome("teste4");
        usuarioDTO.setCpf("98765432110");
        usuarioDTO.setEmail("teste@teste.com");
        usuarioDTO.setTelefone("");
        usuarioDTO.setNivelAcesso("PADRAO");

        String json = objectMapper.writeValueAsString(usuarioDTO);

        mockMvc.perform(put("/usuarios/" + usuario.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("teste4"));
    }

    @Test
    @DisplayName("deve listar todos os usuarios")
    void listarUsuarios() throws Exception {

        mockMvc.perform(get("/usuarios")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Deve deletar usuario")
    void deletarUsuario() throws Exception {

        // pra n precisar fazer "usuario.getId()" onde iria o id dele, poderia por no
        // setup
        Long id = usuario.getId();

        mockMvc.perform(delete("/usuarios/" + id)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        usuario = usuarioRepository.findById(id).orElseThrow();

        assertFalse(usuario.isAtivo());
    }

    /*
     * Reativar usuário inativo —
     * salva usuário com ativo = false pelo repository, faz POST /usuarios com mesmo
     * CPF, verifica que voltou ativo
     * CPF duplicado no update —
     * cria dois usuários, tenta editar um com o CPF do outro, espera 400
     * Email duplicado no update —
     * igual ao CPF mas com email
     */

    @Test
    @DisplayName("Reativar usuário inativo")
    void reativarUsuarioInativo() throws Exception {
        this.usuario = new Usuario();
        usuario.setNome("testeAtivo");
        usuario.setCpf("11122233344");
        usuario.setEmail("tantofaz2@gmail.com");
        usuario.setSenha("123456");
        usuario.setNivelAcesso(NivelAcesso.PADRAO);
        usuario.setAtivo(false);
        this.usuario = usuarioRepository.save(usuario);

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setNome("testeAtivo");
        usuarioDTO.setCpf("11122233344");
        usuarioDTO.setEmail("");
        usuarioDTO.setTelefone("");
        usuarioDTO.setNivelAcesso("PADRAO");

        String json = objectMapper.writeValueAsString(usuarioDTO);

        mockMvc.perform(post("/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value("11122233344"));

        Usuario reativado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertTrue(reativado.isAtivo());
    }

    /*
     * CPF duplicado no update —
     * cria dois usuários, tenta editar um com o CPF do outro, espera 400
     * Email duplicado no update —
     * igual ao CPF mas com email
     */
    

    @Test
    @DisplayName("tentando editar um usuario com cpf ja existente e ativo")
    void editarUsuarioComCpfAtivo() throws Exception {

        Usuario usuarioB = new Usuario();

        usuarioB.setNome("testeCpfDuplo");
        usuarioB.setCpf("11122233344");
        usuarioB.setEmail("tantofaz2@gmail.com");
        usuarioB.setSenha("123456");
        usuarioB.setNivelAcesso(NivelAcesso.PADRAO);
        usuarioB.setAtivo(true);
        usuarioB = usuarioRepository.save(usuarioB);

        // requisicao de edicao usando o msm cpf do usuario setup

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setNome("testeCpfDuplo");
        usuarioDTO.setCpf("12345678910");
        usuarioDTO.setEmail("");
        usuarioDTO.setTelefone("");
        usuarioDTO.setNivelAcesso("PADRAO");

        String json = objectMapper.writeValueAsString(usuarioDTO);

        mockMvc.perform(put("/usuarios/" + usuarioB.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("tentando editar um usuario com email ja existente e ativo")
    void editarUsuarioComEmailAtivo() throws Exception {

        Usuario usuarioB = new Usuario();

        usuarioB.setNome("testeCpfDuplo");
        usuarioB.setCpf("11122233344");
        usuarioB.setEmail("tantofaz2@gmail.com");
        usuarioB.setSenha("123456");
        usuarioB.setNivelAcesso(NivelAcesso.PADRAO);
        usuarioB.setAtivo(true);
        usuarioB = usuarioRepository.save(usuarioB);

        // requisicao de edicao usando o msm email do usuario setup

        UsuarioDTO usuarioDTO = new UsuarioDTO();

        usuarioDTO.setNome("testeCpfDuplo");
        usuarioDTO.setCpf("11122233344");
        usuarioDTO.setEmail("tantofaz@gmail.com");
        usuarioDTO.setTelefone("");
        usuarioDTO.setNivelAcesso("PADRAO");

        String json = objectMapper.writeValueAsString(usuarioDTO);

        mockMvc.perform(put("/usuarios/" + usuarioB.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
