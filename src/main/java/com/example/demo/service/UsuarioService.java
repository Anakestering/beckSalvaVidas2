package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.UsuarioDTO;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
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

        if (dto.getEmail() != null
                && !dto.getEmail().isBlank()
                && usuarioRepository.existsByEmail(dto.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email já cadastrado.");
        }

        
        String cpfLimpo = dto.getCpf().replaceAll("\\D", "");

        if (usuarioRepository.existsByCpf(cpfLimpo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CPF já cadastrado.");
        }

        Usuario usuario = toEntity(dto);

        
        usuario.setCpf(cpfLimpo);

        // senha = 6 primeiros dígitos do CPF
        String senhaPadrao = cpfLimpo.substring(0, 6);

        usuario.setSenha(
                passwordEncoder.encode(senhaPadrao));

        usuario.setNivelAcesso(
                NivelAcesso.valueOf(dto.getNivelAcesso()));

        usuario.setAtivo(true);

        return toDto(usuarioRepository.save(usuario));
    }
}