package com.example.demo.repository;

import com.example.demo.entity.Posto;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostoRepository extends BaseRepository<Posto, Long> {

    
    List<Posto> findByDeletedAtIsNullOrderByAtivoDescNomeAsc();

    @Query(value = """
        SELECT * FROM posto
        WHERE deleted_at IS NULL
        ORDER BY
            ativo DESC,
            REGEXP_REPLACE(nome, '[0-9].*$', ''),
            REGEXP_SUBSTR(nome, '[0-9]+') IS NULL ASC,
            CAST(NULLIF(REGEXP_SUBSTR(nome, '[0-9]+'), '') AS UNSIGNED) ASC,
            nome
        """, nativeQuery = true)
    List<Posto> findAllNaturalOrder();
}