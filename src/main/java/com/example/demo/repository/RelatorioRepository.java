// repository/RelatorioRepository.java
package com.example.demo.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import com.example.demo.entity.Relatorio;

@Repository
public interface RelatorioRepository extends BaseRepository<Relatorio, Long> {

    Optional<Relatorio> findByPostoIdAndData(Long postoId, LocalDate data);

    boolean existsByPostoIdAndData(Long postoId, LocalDate data);
}