package com.example.demo.dto;

import java.util.List;

import lombok.Data;

@Data
public class ResumoResponseDTO {

    List<CheckinResponseDTO> checkins;
    List<CheckoutResponseDTO> checkouts;
    RelatorioResponseDTO relatorio;
    PostoDTO posto;
}
