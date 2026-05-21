package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import jakarta.transaction.Transactional;

@NoRepositoryBean
public interface BaseRepository<E, ID> extends JpaRepository<E, ID> {

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE #{#entityName} e
            SET e.ativo = FALSE,
            e.deletedAt = CURRENT_TIMESTAMP
            WHERE e.id = :id
            """)
    void softDeleteById(ID id);

    @Query("""
                SELECT e FROM #{#entityName} e
                WHERE e.ativo = TRUE
            """)
    @Override
    List<E> findAll();

    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<E> findById(ID id);
}
