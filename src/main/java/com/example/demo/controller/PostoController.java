package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PostoDTO;
import com.example.demo.service.BaseService;
import com.example.demo.service.PostoService;


@RestController
@RequestMapping("/postos")
public class PostoController extends BaseController<PostoDTO> {

    private final PostoService postoService;

    public PostoController(PostoService service) {
        super((BaseService<?, PostoDTO>) service);
        this.postoService = service;
    }

    @GetMapping("/ordenados")
    public List<PostoDTO> listarOrdenados() {
        return postoService.listarOrdenados();
    }

    @PatchMapping("/{id}/ativo")
    public PostoDTO alternarAtivo(@PathVariable Long id) {
        return postoService.alternarAtivo(id);
    }
}