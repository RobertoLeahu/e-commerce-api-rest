package com.api.services.impl;

import com.api.domain.models.Categoria;
import com.api.dto.response.CategoriaResponseDTO;
import com.api.exceptions.CategoryNotFoundException;
import com.api.mapper.CategoriaMapper;
import com.api.repositories.CategoriaRepository;
import com.api.services.CategoriaService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Cacheable(value = "listaCategorias", key = "'todas'")
    @Override
    public List<CategoriaResponseDTO> obtenerCategorias() {
        List<Categoria> categorias = categoriaRepository.findAll();
        return categorias.stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "productos", key = "#id")
    @Override
    public CategoriaResponseDTO obtenerCategoriaPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id);

        if (categoria == null) {
            throw new CategoryNotFoundException(id);
        }

        return categoriaMapper.toResponse(categoria);
    }
}