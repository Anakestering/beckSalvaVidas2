package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.annotations.Admin;
import com.example.demo.dto.PostoDTO;
import com.example.demo.service.PostoService;

@Admin
@RestController
@RequestMapping("/postos")
public class PostoController extends BaseController<PostoDTO> {

    public PostoController(PostoService service){
        super(service);
    }

}
