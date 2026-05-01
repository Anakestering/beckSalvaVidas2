package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Checkin;

@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    // Buscar todos registros de um posto
    List<Checkin> findByPosto(Long postoId);

    // Buscar por período (muito importante)
    List<Checkin> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

    List<Checkin> findByPostoIdAndDataHoraBetween(
            Long postoId,
            LocalDateTime inicio,
            LocalDateTime fim);

    @Query("""
                SELECT c FROM Checkin c
                JOIN c.posto p
                WHERE c.visivelAdmin = true
                ORDER BY
                    CASE WHEN p.ordem IS NULL THEN 1 ELSE 0 END,
                    p.ordem ASC,
                    c.dataHora ASC
            """)
    List<Checkin> buscarOrdenadosPorPosto();

    boolean existsByPostoIdAndDataHoraBetween(Long postoId, LocalDateTime inicio, LocalDateTime fim);

}
