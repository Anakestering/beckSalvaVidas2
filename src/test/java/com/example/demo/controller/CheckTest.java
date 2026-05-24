package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.config.JwtUtil;
import com.example.demo.entity.Arquivo;
import com.example.demo.entity.Checkin;
import com.example.demo.entity.Checkout;
import com.example.demo.entity.Posto;
import com.example.demo.entity.Relatorio;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.CheckinRepository;
import com.example.demo.repository.CheckoutRepository;
import com.example.demo.repository.PostoRepository;
import com.example.demo.repository.RelatorioRepository;
import com.example.demo.service.ArquivoService;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class CheckTest {
        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext context;

        @Autowired
        private JwtUtil jwt;
        private String token;

        @Autowired
        private PostoRepository postoRepository;
        private Posto posto;

        @Autowired
        private ArquivoService arquivoService;
        private Arquivo arquivo;

        @Autowired
        private CheckinRepository checkinRepository;
        private Checkin checkin;

        @Autowired
        private CheckoutRepository checkoutRepository;
        private Checkout checkout;

        @Autowired
        private RelatorioRepository relatorioRepository;
        private Relatorio relatorio;

        // =============== SETUP ====================

        @BeforeEach
        public void setup() {

                // prepara pra requisições por admin
                this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
                this.token = jwt.generateToken("teste@admin.com", NivelAcesso.ADMIN.toString());

                // cria posto
                this.posto = new Posto();
                posto.setNome("PostoTeste");
                posto.setDescricao("Posto teste");

                posto = postoRepository.save(posto);

                // cria o arquivo
                MockMultipartFile fotoMock = new MockMultipartFile(
                                "foto", "bombeiro.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "conteudo".getBytes());
                this.arquivo = arquivoService.upload(fotoMock);

                // cria o checkin
                this.checkin = new Checkin();
                checkin.setPosto(posto);
                // seta o arquivo criado antes
                checkin.setFoto(arquivo);
                // com o horario agora
                checkin.setDataHora(LocalDateTime.now());

                this.checkin = checkinRepository.save(checkin);

                // cria relatório
                this.relatorio = new Relatorio();
                relatorio.setPosto(posto);
                relatorio.setData(LocalDate.now());
                relatorio.setAtaquesManha(20);
                relatorio.setPrevencoesManha(10);
                relatorio.setAtaquesTarde(20);
                relatorio.setPrevencoesTarde(10);
                relatorio.setObservacoes("teste");

                this.relatorio = relatorioRepository.save(relatorio);

                // cria um checkout
                this.checkout = new Checkout();
                checkout.setPosto(posto);
                checkout.setFoto(arquivo);
                checkout.setDataHora(LocalDateTime.now());

                this.checkout = checkoutRepository.save(checkout);

        }

        // ================== CHECKIN =======================

        @Test
        @DisplayName("Fazer checkin")
        void checkin() throws Exception {
                MockMultipartFile fotoMock = new MockMultipartFile("foto", "bombeiro.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

                mockMvc.perform(multipart("/check/in").file(fotoMock).param("postoId", posto.getId().toString())
                                .header("Authorization", "Bearer " + token)).andExpect(status().isOk())
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Fazer checkin sem vincular um posto")
        void checkinSemPosto() throws Exception {
                MockMultipartFile fotoMock = new MockMultipartFile("foto", "bombeiro.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

                mockMvc.perform(multipart("/check/in").file(fotoMock)
                                .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest())
                                .andExpect(status().isBadRequest());
        }

        // ================== ocultar todos =============
        @Test
        @DisplayName("ocultar todos checkin como admin")
        void ocultarTodosComoAdmin() throws Exception {

                mockMvc.perform(patch("/check/in/ocultar-todos")
                                .header("Authorization", "Bearer " + token)
                                .header("NivelAcesso", "ADMIN"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ocultar todos sem permissao")
        void ocultarTodosSemPermissao() throws Exception {

                String tokenPadrao = jwt.generateToken("teste@teste.com", NivelAcesso.PADRAO.toString());

                mockMvc.perform(patch("/check/in/ocultar-todos")
                                .header("Authorization", "Bearer " + tokenPadrao)
                                .header("NivelAcesso", "PADRAO"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("tentar ocultar sem token")
        void ocultarTodosSemToken() throws Exception {

                mockMvc.perform(patch("/check/in/ocultar-todos"))
                                .andExpect(status().isUnauthorized());
        }

        // ============================= ocultar por id ==========================

        @Test
        @DisplayName("ocultar por id como admin")
        void ocultarPorIdComoAdmin() throws Exception {

                mockMvc.perform(patch("/check/in/ocultar/" + checkin.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("NivelAcesso", "ADMIN"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ocultar por id sem permissao")
        void ocultarPorIdSemPermissao() throws Exception {

                String tokenPadrao = jwt.generateToken("teste@teste.com", NivelAcesso.PADRAO.toString());

                mockMvc.perform(patch("/check/in/ocultar/" + checkin.getId())
                                .header("Authorization", "Bearer " + tokenPadrao)
                                .header("NivelAcesso", "PADRAO"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("tentar ocultar sem token por id")
        void ocultarPorIdSemToken() throws Exception {

                mockMvc.perform(patch("/check/in/ocultar/" + checkin.getId()))
                                .andExpect(status().isUnauthorized());
        }

        // ================ Listar checkin ==========

        @Test
        @DisplayName("deve retornar uma lista de checkins")
        void listarCheckins() throws Exception {
                mockMvc.perform(get("/check/in")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        // ======= busca diaria de checkin por posto =========

        @Test
        @DisplayName("busca o checkin do dia do posto expecifico")
        void buscarHojePorPosto() throws Exception {
                mockMvc.perform(get("/check/in/hoje/" + checkin.getPosto().getId())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].posto").value(posto.getNome()));
        }

        // =================== CHECKOUT ===========================
        @Test
        @DisplayName("Fazer checkout")
        void checkout() throws Exception {
                MockMultipartFile fotoMock = new MockMultipartFile("foto", "bombeiro.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

                mockMvc.perform(multipart("/check/out").file(fotoMock)
                                .param("postoId", posto.getId().toString())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Fazer checkout sem vincular um posto")
        void checkoutSemPosto() throws Exception {
                MockMultipartFile fotoMock = new MockMultipartFile("foto", "bombeiro.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

                mockMvc.perform(multipart("/check/out").file(fotoMock)
                                .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
        }

        /*
         * Checkout sem checkin — cria um posto novo sem checkin e tenta fazer checkout
         * Checkout sem relatório — cria um posto com checkin mas sem relatório e tenta
         * fazer checkout
         */

        @Test
        @DisplayName("tentar fazer checkout sem checkin")
        void checkoutSemCheckin() throws Exception {
                Posto postoSemCheckin = new Posto();
                postoSemCheckin.setNome("postoSemCheckin");
                Posto postoSalvo = postoRepository.save(postoSemCheckin);

                MockMultipartFile fotoMock = new MockMultipartFile("foto", "bombeiro.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

                assertThrows(Exception.class, () -> 
                mockMvc.perform(multipart("/check/out").file(fotoMock)
                                .param("postoId", postoSalvo.getId().toString())
                                .header("Authorization", "Bearer " + token)));
        }

        @Test
        @DisplayName("tentar fazer checkout sem relatorio")
        void checkoutSemRelatorio() throws Exception {
                //cria oosto
                Posto postoSemRelatorio = new Posto();
                postoSemRelatorio.setNome("postoSemRelatorio");
                Posto postoSalvo = postoRepository.save(postoSemRelatorio);

                //cria arquivo pra fazer checkin
                 MockMultipartFile fotoMock = new MockMultipartFile(
                                "foto", "bombeiro.jpg",
                                MediaType.IMAGE_JPEG_VALUE,
                                "conteudo".getBytes());
                this.arquivo = arquivoService.upload(fotoMock);

                // cria o checkin
                this.checkin = new Checkin();
                checkin.setPosto(postoSalvo);
                // seta o arquivo criado antes
                checkin.setFoto(arquivo);
                checkin.setDataHora(LocalDateTime.now());
                this.checkin = checkinRepository.save(checkin);


                //agr tenta fazer checkout apenas sem relatorio
                MockMultipartFile fotoMockCheckout = new MockMultipartFile("foto", "bombeiro2.jpg",
                                MediaType.IMAGE_JPEG_VALUE, "conteudo".getBytes());

                assertThrows(Exception.class, () -> 
                mockMvc.perform(multipart("/check/out").file(fotoMockCheckout)
                                .param("postoId", postoSalvo.getId().toString())
                                .header("Authorization", "Bearer " + token)));
        }


        
        // ================== ocultar todos =============
        @Test
        @DisplayName("ocultar todos checkout como admin")
        void ocultarTodosCheckoutComoAdmin() throws Exception {

                mockMvc.perform(patch("/check/out/ocultar-todos")
                                .header("Authorization", "Bearer " + token)
                                .header("NivelAcesso", "ADMIN"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ocultar todos checkout sem permissao")
        void ocultarTodosCheckoutSemPermissao() throws Exception {

                String tokenPadrao = jwt.generateToken("teste@teste.com", NivelAcesso.PADRAO.toString());

                mockMvc.perform(patch("/check/out/ocultar-todos")
                                .header("Authorization", "Bearer " + tokenPadrao)
                                .header("NivelAcesso", "PADRAO"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("tentar ocultar sem token")
        void ocultarTodosCheckoutSemToken() throws Exception {

                mockMvc.perform(patch("/check/out/ocultar-todos"))
                                .andExpect(status().isUnauthorized());
        }

        // ============================= ocultar por id ==========================

        @Test
        @DisplayName("ocultar por id como admin")
        void ocultarCheckoutPorIdComoAdmin() throws Exception {

                mockMvc.perform(patch("/check/out/ocultar/" + checkout.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("NivelAcesso", "ADMIN"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("ocultar por id sem permissao")
        void ocultarCheckoutPorIdSemPermissao() throws Exception {

                String tokenPadrao = jwt.generateToken("teste@teste.com", NivelAcesso.PADRAO.toString());

                mockMvc.perform(patch("/check/out/ocultar/" + checkout.getId())
                                .header("Authorization", "Bearer " + tokenPadrao)
                                .header("NivelAcesso", "PADRAO"))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("tentar ocultar sem token por id")
        void ocultarCheckoutPorIdSemToken() throws Exception {

                mockMvc.perform(patch("/check/out/ocultar/" + checkout.getId()))
                                .andExpect(status().isUnauthorized());
        }

        // ================ Listar checkout ==========

        @Test
        @DisplayName("deve retornar uma lista de checkouts")
        void listarCheckouts() throws Exception {
                mockMvc.perform(get("/check/out")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        // ======= busca diaria de checkout por posto =========

        @Test
        @DisplayName("busca o checkout do dia do posto expecifico")
        void buscarCheckoutHojePorPosto() throws Exception {
                mockMvc.perform(get("/check/out/hoje/" + checkout.getPosto().getId())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].posto").value(posto.getNome()));
        }

        // ================= DELETAR TODOS =========================

        @Test
        @DisplayName("deeltar todos check de uma vez")
        void deletarTodosArquivos() throws Exception {
                mockMvc.perform(delete("/check/todos")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk());

                // verifica se foi deletado
                assertTrue(checkinRepository.findAll().isEmpty());
                assertTrue(checkoutRepository.findAll().isEmpty());

        }

        // ====== deleta sem permissao ==========
        @Test
        @DisplayName("deleta sem permissao")
        void deletaSemPermissao() throws Exception {

                String tokenPadrao = jwt.generateToken("teste@teste.com", NivelAcesso.PADRAO.toString());

                mockMvc.perform(delete("/check/todos")
                                .header("Authorization", "Bearer " + tokenPadrao)
                                .header("NivelAcesso", "PADRAO"))
                                .andExpect(status().isForbidden());

                // verifica se de fato ta la
                assertFalse(checkinRepository.findAll().isEmpty());
                assertFalse(checkoutRepository.findAll().isEmpty());
        }

        // ============== deletar sem token ============

        @Test
        @DisplayName("tentar deletar sem token")
        void deletarSemToken() throws Exception {

                mockMvc.perform(delete("/check/todos"))
                                .andExpect(status().isUnauthorized());

                // verifica se de fato ta la
                assertFalse(checkinRepository.findAll().isEmpty());
                assertFalse(checkoutRepository.findAll().isEmpty());
        }

}