package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.UsuarioRepository;

@Service
public class UsuarioService extends BaseService<Usuario, UsuarioDTO> {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.usuarioRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UsuarioDTO create(UsuarioDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado.");
        }

        Usuario usuario = toEntity(dto);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));

        return toDto(usuarioRepository.save(usuario));
    }
}