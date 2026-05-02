package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Checkout;

@Repository
public interface CheckoutRepository extends BaseRepository<Checkout, Long> {

        List<Checkout> findByPostoId(Long postoId);

        List<Checkout> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);

        List<Checkout> findByPostoIdAndDataHoraBetween(
                        Long postoId,
                        LocalDateTime inicio,
                        LocalDateTime fim);

        @Query("""
                            SELECT c FROM Checkout c
                            JOIN c.posto p
                            WHERE c.visivelAdmin = true
                            ORDER BY p.id ASC, c.dataHora ASC
                        """)
        List<Checkout> buscarOrdenadosPorPosto();

}
