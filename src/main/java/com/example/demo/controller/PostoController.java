package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PostoDTO;
import com.example.demo.dto.PostoStatusDTO;
import com.example.demo.service.BaseService;
import com.example.demo.service.PostoService;

@RestController
@RequestMapping("/postos")
// está dizendo que esse controller trabalha com dados do tipo PostoDTO.
public class PostoController extends BaseController<PostoDTO> {

      private final PostoService postoService;

      // conecta service com controller.
      public PostoController(PostoService service) {
            super((BaseService<?, PostoDTO>) service);
            this.postoService = service;
      }

      @GetMapping("/ordenados")
      // esse método devolve uma lista de PostoDTO.
      public List<PostoDTO> listarOrdenados() {

            // pede pro Service fazer o trabalho e devolve o resultado.
            return postoService.listarOrdenados();
      }

      @GetMapping("/status-hoje")
      public List<PostoStatusDTO> statusHoje() {
            return postoService.buscarStatusPostos();
      }

      @PatchMapping("/{id}/ativo")
      // @PathVariable Long id — está capturando esse {id} da URL e guardando na
      // variável id.
      public PostoDTO alternarAtivo(@PathVariable Long id) {
            return postoService.alternarAtivo(id);
      }
}
/*
 * FRONT-END chama /postos/ordenados
 * ↓
 * CONTROLLER recebe
 * ↓
 * postoService.listarOrdenados() ← liga pro service
 * ↓
 * SERVICE faz o trabalho e devolve a lista
 * ↓
 * CONTROLLER pega e devolve pro front-end
 * ↓
 * FRONT-END exibe
 */