// dto/RelatorioDTO.java
package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RelatorioDTO {

    @NotNull(message = "O ID do posto é obrigatório.")
    private Long postoId;

    @Min(0) private int ataquesManha;
    @Min(0) private int prevencoesManha;
    @Min(0) private int ataquesTarde;
    @Min(0) private int prevencoesTarde;

    private String observacoes;
}