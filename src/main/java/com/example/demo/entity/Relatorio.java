// entity/Relatorio.java
package com.example.demo.entity;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "relatorios")
@EqualsAndHashCode(callSuper = false)
public class Relatorio extends BaseEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Posto posto;

    @Column(nullable = false)
    private LocalDate data;

    // Manhã
    @Column(nullable = false)
    private int ataquesManha = 0;

    @Column(nullable = false)
    private int prevencoesManha = 0;

    // Tarde
    @Column(nullable = false)
    private int ataquesTarde = 0;

    @Column(nullable = false)
    private int prevencoesTarde = 0;

    @Column(length = 1000)
    private String observacoes;
}