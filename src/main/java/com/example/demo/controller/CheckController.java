// CheckController.java — rotas separadas para checkin e checkout
package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.CheckinDTO;
import com.example.demo.dto.CheckinResponseDTO;
import com.example.demo.dto.CheckoutDTO;
import com.example.demo.dto.CheckoutResponseDTO;
import com.example.demo.service.CheckService;
import com.example.demo.service.CheckoutService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/check")
public class CheckController {

    @Autowired
    private CheckService checkService;

    @Autowired
    private CheckoutService checkoutService;

    // ─── Checkin ────────────────────────────────────────────

    @PostMapping("/in")
    public CheckinResponseDTO checkin(@ModelAttribute @Valid CheckinDTO dto) {
        return checkService.checkin(dto);
    }

    @GetMapping("/in")
    public List<CheckinDTO> listarCheckins() {
        return checkService.listarTodos();
    }

    @PatchMapping("/in/ocultar-todos")
    public void ocultarTodosCheckins() {
        checkService.ocultarTodos();
    }

    @PatchMapping("/in/ocultar/{id}")
    public void ocultarCheckin(@PathVariable Long id) {
        checkService.ocultar(id);
    }

    @GetMapping("/in/hoje/{postoId}")
    public List<CheckinDTO> buscarCheckinsHoje(@PathVariable Long postoId) {
        return checkService.buscarHoje(postoId);
    }

    // ─── Checkout ───────────────────────────────────────────

    @PostMapping("/out")
    public CheckoutResponseDTO checkout(@ModelAttribute @Valid CheckoutDTO dto) {
        return checkoutService.checkout(dto);
    }

    @GetMapping("/out")
    public List<CheckoutDTO> listarCheckouts() {
        return checkoutService.listarTodos();
    }

    @PatchMapping("/out/ocultar-todos")
    public void ocultarTodosCheckouts() {
        checkoutService.ocultarTodos();
    }

    @PatchMapping("/out/ocultar/{id}")
    public void ocultarCheckout(@PathVariable Long id) {
        checkoutService.ocultar(id);
    }

    @GetMapping("/out/hoje/{postoId}")
    public List<CheckoutDTO> buscarCheckoutsHoje(@PathVariable Long postoId) {
        return checkoutService.buscarHoje(postoId);
    }
}

