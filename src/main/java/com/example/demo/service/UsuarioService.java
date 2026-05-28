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

    private String normalizarCampoOpcional(String valor) {
        return valor != null && valor.isBlank() ? null : valor;
    }

    @Override
    public UsuarioDTO create(UsuarioDTO dto) {
        String cpfLimpo = dto.getCpf().replaceAll("\\D", "");

        dto.setEmail(normalizarCampoOpcional(dto.getEmail()));
        dto.setTelefone(normalizarCampoOpcional(dto.getTelefone()));

        // verifica se existe inativo com esse CPF e reativa
        Optional<Usuario> inativo = usuarioRepository.findByCpfAndAtivoFalse(cpfLimpo);
        if (inativo.isPresent()) {
            Usuario usuario = inativo.get();
            usuario.setNome(dto.getNome());
            usuario.setEmail(dto.getEmail());
            usuario.setTelefone(dto.getTelefone());
            usuario.setSenha(passwordEncoder.encode(cpfLimpo.substring(0, 6)));
            usuario.setNivelAcesso(NivelAcesso.valueOf(dto.getNivelAcesso()));
            usuario.setAtivo(true);
            return toDto(usuarioRepository.save(usuario));
        }

        // verifica email duplicado
        if (dto.getEmail() != null && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email já cadastrado.");
        }

        // verifica se existe ativo com esse CPF e barra
        if (usuarioRepository.existsByCpf(cpfLimpo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF já cadastrado.");
        }

        // cria novo
        Usuario usuario = toEntity(dto);
        usuario.setCpf(cpfLimpo);
        usuario.setSenha(passwordEncoder.encode(cpfLimpo.substring(0, 6)));
        usuario.setNivelAcesso(NivelAcesso.valueOf(dto.getNivelAcesso()));
        usuario.setAtivo(true);

        return toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO update(Long id, UsuarioDTO dto) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."));

        String cpfLimpo = dto.getCpf().replaceAll("\\D", "");
        String email = normalizarCampoOpcional(dto.getEmail());
        String telefone = normalizarCampoOpcional(dto.getTelefone());

        // verifica CPF duplicado
        if (!usuario.getCpf().equals(cpfLimpo)
                && usuarioRepository.existsByCpf(cpfLimpo)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CPF já cadastrado.");
        }

        // verifica email duplicado
        if (email != null
                && !email.equals(usuario.getEmail())
                && usuarioRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email já cadastrado.");
        }

        usuario.setNome(dto.getNome());
        usuario.setCpf(cpfLimpo);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setNivelAcesso(
                NivelAcesso.valueOf(dto.getNivelAcesso()));

        return toDto(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO toDto(Usuario entity) {
        UsuarioDTO dto = super.toDto(entity);
        dto.setNivelAcesso(entity.getNivelAcesso() != null ? entity.getNivelAcesso().name() : null);

        return dto;
    }

}