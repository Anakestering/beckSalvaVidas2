package com.example.demo.service;

import java.util.Optional;

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

        String email = normalizar(dto.getEmail());
        String cpf   = dto.getCpf().replaceAll("[^0-9]", "");

        // verifica se existe um usuário INATIVO com esse CPF
        // se sim, reativa e atualiza os dados em vez de criar duplicata
        Optional<Usuario> inativo = usuarioRepository.findByCpfIncludingInactive(cpf);
        if (inativo.isPresent()) {
            Usuario usuario = inativo.get();

            // valida email só se informado e diferente do atual
            if (email != null && !email.equals(usuario.getEmail())) {
                if (usuarioRepository.existsByEmail(email)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado.");
                }
            }

            usuario.setNome(dto.getNome());
            usuario.setCpf(cpf);
            usuario.setEmail(email);
            usuario.setTelefone(normalizar(dto.getTelefone()));
            usuario.setNivelAcesso(NivelAcesso.valueOf(dto.getNivelAcesso()));
            usuario.setAtivo(true);
            usuario.setDeletedAt(null);

            // reseta a senha para os 6 primeiros dígitos do CPF
            usuario.setSenha(passwordEncoder.encode(cpf.substring(0, 6)));

            return toDto(usuarioRepository.save(usuario));
        }

        // CPF novo — fluxo normal
        if (email != null && usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado.");
        }
        if (usuarioRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF já cadastrado.");
        }

        Usuario usuario = toEntity(dto);
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setTelefone(normalizar(dto.getTelefone()));
        usuario.setSenha(passwordEncoder.encode(cpf.substring(0, 6)));
        usuario.setNivelAcesso(NivelAcesso.valueOf(dto.getNivelAcesso()));

        return toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO update(Long id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();

        String email = normalizar(dto.getEmail());

        if (email != null && !email.equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(email)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado.");
            }
        }

        usuario.setNome(dto.getNome());
        usuario.setCpf(dto.getCpf().replaceAll("[^0-9]", ""));
        usuario.setEmail(email);
        usuario.setTelefone(normalizar(dto.getTelefone()));
        usuario.setNivelAcesso(NivelAcesso.valueOf(dto.getNivelAcesso()));

        return toDto(usuarioRepository.save(usuario));
    }

    /**
     * Retorna null se a string for null ou vazia.
     * Evita inserir strings vazias em colunas com constraint UNIQUE.
     */
    private String normalizar(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}