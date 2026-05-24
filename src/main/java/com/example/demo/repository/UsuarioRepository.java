package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    @Query("""
            SELECT u FROM Usuario u
            WHERE u.cpf = :cpf
            AND u.ativo = TRUE
    """)
    Optional<Usuario> findByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpf(String cpf);

    //para o initializer
    List<Usuario> findByNivelAcesso(NivelAcesso nivelAcesso);

    Optional<Usuario> findByCpfAndAtivoFalse(String cpf);

}
