package com.example.demo.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class CheckinResponseDTO {
    
    private Long id; 
    private String posto;

    private String foto;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime horario;
}
