package com.example.demo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.Arquivo;
import com.example.demo.repository.ArquivoRepository;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Service
public class ArquivoService {

    @Value("${arquivamento.path}")
    private String path;

    @Autowired
    private ArquivoRepository arquivoRepository;

    public Arquivo upload(MultipartFile file) {
        Path root = Paths.get(path);

        try {
            if (!Files.exists(root)) {

                Files.createDirectories(root);
            }

            String nomeOriginal = file.getOriginalFilename();

            if (nomeOriginal != null && nomeOriginal.contains(".")) {
                nomeOriginal.substring(nomeOriginal.lastIndexOf("."));
            }

            String nome = UUID.randomUUID().toString();

            Path destino = root.resolve(nome);

            Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            Arquivo arquivo = new Arquivo();

            arquivo.setCaminho(destino.toString());
            arquivo.setNome(file.getOriginalFilename());
            arquivo.setTamanho(file.getSize());
            arquivo.setTipo(file.getContentType());

            return arquivoRepository.save(arquivo);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar arquivo", e);
        }
    }

    public ResponseEntity<Resource> servir(String nome) {
        try {
            Path arquivo = Paths.get(path).resolve(nome);
            Resource resource = new UrlResource(arquivo.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo não encontrado: " + nome);
            }

            String contentType = Files.probeContentType(arquivo);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo", e);
        }

    }

    public String montarUrl(Arquivo foto) {
        if (foto == null)
            return null;
        String nome = Paths.get(foto.getCaminho()).getFileName().toString();
        return "http://localhost:8080/arquivos/" + nome;
    }

}
