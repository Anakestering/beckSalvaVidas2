
package com.example.demo.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class RelatorioResponseDTO {
    private Long postoId;
     private String posto; 
    private LocalDate data;
    private int ataquesManha;
    private int prevencoesManha;
    private int ataquesTarde;
    private int prevencoesTarde;
    private String observacoes;
}