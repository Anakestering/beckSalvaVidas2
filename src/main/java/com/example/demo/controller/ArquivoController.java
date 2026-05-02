package com.example.demo.controller;

import org.springframework.core.io.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Public;
import com.example.demo.service.ArquivoService;

@Public
@RestController
@RequestMapping("/arquivos")
public class ArquivoController {

    @Autowired
    private ArquivoService arquivoService;

    @GetMapping("/{nome}")
    public ResponseEntity<Resource> servir(@PathVariable String nome) {
        return arquivoService.servir(nome);
    }
}