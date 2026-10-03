package com.api.controllers;

import com.api.dto.request.ProductoRequestDTO;
import com.api.dto.response.ProductoResponseDTO;
import com.api.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<ProductoResponseDTO> guardarProducto(@Valid @RequestBody ProductoRequestDTO productoRequestDTO) {
        ProductoResponseDTO productoGuardado = productoService.guardarProducto(productoRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(productoGuardado);
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductos() {
        List<ProductoResponseDTO> productos = productoService.obtenerProductos();
        return ResponseEntity.ok(productos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProductoPorId(@PathVariable Long id) {
        ProductoResponseDTO producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrarProducto(@PathVariable Long id) {
        String productoBorrado = productoService.borrarProducto(id);
        return ResponseEntity.ok(productoBorrado);
    }
}