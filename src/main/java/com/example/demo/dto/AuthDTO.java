package com.example.demo.dto;

import org.hibernate.validator.constraints.br.CPF;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthDTO {
    
    @NotBlank(message = "O cpf deve ser preenchido.")
    private String cpf;

    @NotBlank(message = "A senha deve ser preenchido.")
    private String senha;

}
