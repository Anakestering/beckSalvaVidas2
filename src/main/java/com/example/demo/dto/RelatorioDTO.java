
package com.example.demo.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RelatorioDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id; 
    
    @NotNull(message = "O ID do posto é obrigatório.")
    private Long postoId;

    @Min(0) private int ataquesManha;
    @Min(0) private int prevencoesManha;
    @Min(0) private int ataquesTarde;
    @Min(0) private int prevencoesTarde;

    private String observacoes;
}