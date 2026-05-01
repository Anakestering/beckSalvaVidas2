
package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Relatorio;

@Repository
public interface RelatorioRepository extends BaseRepository<Relatorio, Long> {

    Optional<Relatorio> findByPostoIdAndData(Long postoId, LocalDate data);

    boolean existsByPostoIdAndData(Long postoId, LocalDate data);

     
    List<Relatorio> findByPostoId(Long postoId);

    @Query("""
                SELECT r FROM Relatorio r
                JOIN r.posto p
                WHERE r.visivelAdmin = true
                ORDER BY
                    CASE WHEN p.id IS NULL THEN 1 ELSE 0 END,
                    p.id ASC
            """)
    List<Relatorio> findAllByOrderByPosto_IdAsc();

}