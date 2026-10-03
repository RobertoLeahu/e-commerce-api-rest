package com.api.services.impl;

import com.api.domain.models.Categoria;
import com.api.domain.models.Producto;
import com.api.dto.request.ProductoRequestDTO;
import com.api.dto.response.ProductoResponseDTO;
import com.api.exceptions.CategoryNotFoundException;
import com.api.exceptions.ProductNotFoundException;
import com.api.mapper.ProductoMapper;
import com.api.repositories.CategoriaRepository;
import com.api.repositories.ProductoRepository;
import com.api.services.ProductoService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoServiceImpl implements ProductoService {

    final private ProductoRepository productoRepository;
    final private ProductoMapper productoMapper;
    final private CategoriaRepository categoriaRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository, ProductoMapper productoMapper, CategoriaRepository categoriaRepository){
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
        this.categoriaRepository = categoriaRepository;
    }

    @Caching(
            put = { @CachePut(value = "productos", key = "#result.id") },
            evict = { @CacheEvict(value = "listaProductos", allEntries = true) }
    )
    @Override
    public ProductoResponseDTO guardarProducto(ProductoRequestDTO productoRequestDTO) {

        Long categoriaId = productoRequestDTO.categoriaId();

        Categoria categoria = categoriaRepository.findById(categoriaId);
        if (categoria == null) {
            throw new CategoryNotFoundException(categoriaId);
        }

        Producto productoNuevo = productoMapper.toEntity(productoRequestDTO, categoria);
        Producto productoGuardado = productoRepository.save(productoNuevo);
        return productoMapper.toResponse(productoGuardado);
    }

    @Cacheable(value = "productos", key = "#id")
    @Override
    public ProductoResponseDTO obtenerProductoPorId(Long id) {

        delay(500);

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productoMapper.toResponse(producto);
    }

    @Cacheable(value = "listaProductos", key = "'todos'")
    @Override
    public List<ProductoResponseDTO> obtenerProductos() {

        delay(500);

        List<Producto> productos = productoRepository.findAll();
        return productos.stream()
                .map(productoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Caching(evict = {
            @CacheEvict(value = "productos", key = "#id"),
            @CacheEvict(value = "listaProductos", allEntries = true)
    })
    @Override
    public String borrarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        productoRepository.deleteById(id);
        return "Producto con id " + id + " borrado correctamente";
    }

    // Metodo auxiliar para añadir delay a las funciones
    private void delay(int milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            // Restaura el estado de interrupción por buena práctica en hilos
            Thread.currentThread().interrupt();
        }
    }
}
