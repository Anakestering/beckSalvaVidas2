package com.example.demo.repository;

import org.springframework.stereotype.Repository;
import java.util.List;

import com.example.demo.entity.Posto;

@Repository
public interface PostoRepository extends BaseRepository<Posto, Long> {

    List<Posto> findByDeletedAtIsNullOrderByAtivoDescNomeAsc();

}
