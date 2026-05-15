package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.entity.Usuario;

@Repository
public interface UsuarioRepository extends BaseRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.email = :email AND u.ativo = TRUE")
    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.cpf = :cpf AND u.ativo = TRUE")
    Optional<Usuario> findByCpf(String cpf);

    // busca CPF apenas entre ativos — usado nas validações de unicidade
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.email = :email AND u.ativo = TRUE")
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.cpf = :cpf AND u.ativo = TRUE")
    boolean existsByCpf(String cpf);

    // busca CPF incluindo inativos — usado para reativação no create
    @Query(nativeQuery = true, value = "SELECT * FROM usuario WHERE cpf = :cpf AND ativo = FALSE")
    Optional<Usuario> findByCpfIncludingInactive(String cpf);
}