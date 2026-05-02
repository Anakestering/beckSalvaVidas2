package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.RelatorioDTO;
import com.example.demo.dto.RelatorioResponseDTO;
import com.example.demo.service.RelatorioService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/relatorio")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @PostMapping
    public RelatorioResponseDTO salvar(@RequestBody @Valid RelatorioDTO dto) {
        return relatorioService.salvar(dto);
    }

    @GetMapping
    public List<RelatorioResponseDTO> listarTodos() {
        return relatorioService.listarTodos();
    }

    @Admin
    @PatchMapping("/ocultar-todos")
    public void ocultarTodos() {
        relatorioService.ocultarTodos();
    }

    @Admin
    @PatchMapping("/ocultar/{id}")
    public void ocultar(@PathVariable Long id) {
        relatorioService.ocultar(id);
    }

    @GetMapping("/hoje/{postoId}")
    public RelatorioResponseDTO buscarHoje(@PathVariable Long postoId) {
        return relatorioService.buscarHoje(postoId);
    }

    @GetMapping("/posto/{postoId}")
    public List<RelatorioResponseDTO> listarPorPosto(@PathVariable Long postoId) {
        return relatorioService.listarPorPosto(postoId);
    }

    @Admin
    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return relatorioService.exportarExcel(inicio, fim);
    }
}