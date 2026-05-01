
package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.RelatorioDTO;
import com.example.demo.dto.RelatorioResponseDTO;
import com.example.demo.service.RelatorioService;

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
}