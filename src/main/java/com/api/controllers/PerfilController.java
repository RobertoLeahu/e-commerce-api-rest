package com.api.controllers;

import com.api.dto.request.PerfilRequestDTO;
import com.api.dto.response.PerfilResponseDTO;
import com.api.services.impl.PerfilServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perfiles")
public class PerfilController {

    final private PerfilServiceImpl perfilService;

    public PerfilController(PerfilServiceImpl perfilService) {
        this.perfilService = perfilService;
    }

    @PostMapping
    public ResponseEntity<PerfilResponseDTO> crearPerfil(@Valid @RequestBody PerfilRequestDTO perfilRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilService.crearPerfil(perfilRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<PerfilResponseDTO>> obtenerPerfiles() {
        return ResponseEntity.ok(perfilService.obtenerPerfiles());
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<PerfilResponseDTO> obtenerPerfilPorIdDeUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(perfilService.obtenerPerfilPorIdDeUsuario(idUsuario));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<PerfilResponseDTO> actualizarPerfil(@PathVariable Long idUsuario, @Valid @RequestBody PerfilRequestDTO perfilRequestDTO) {
        return ResponseEntity.ok(perfilService.actualizarPerfil(idUsuario, perfilRequestDTO));
    }
}
