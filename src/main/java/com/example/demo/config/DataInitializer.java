package com.example.demo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.entity.Usuario;
import com.example.demo.enums.NivelAcesso;
import com.example.demo.repository.UsuarioRepository;

@Configuration
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    public DataInitializer(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository repository){
        return args -> {
            if(repository.count() <= 0){
                Usuario usuario = new Usuario();

                usuario.setNome("Administrador");
                usuario.setCpf("11111111111");
                usuario.setEmail("admin@admin.com");
                usuario.setNivelAcesso(NivelAcesso.ADMIN);
                usuario.setSenha(passwordEncoder.encode("111111"));


                repository.save(usuario);


                usuario.setNome("usuarioPadrao");
                usuario.setCpf("22222222222");
                usuario.setEmail("user@user.com");
                usuario.setNivelAcesso(NivelAcesso.PADRAO);
                usuario.setSenha(passwordEncoder.encode("222222"));

                repository.save(usuario);

                System.out.println("Usuário ADMIN e PADRAO criado com sucesso");
            }else{
                System.out.println("Usuário ADMIN e PADRAO já existe no banco!");
            }
        };
    }
    

}
