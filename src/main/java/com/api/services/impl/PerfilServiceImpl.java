package com.api.services.impl;

import com.api.domain.models.Perfil;
import com.api.domain.models.Usuario;
import com.api.dto.request.PerfilRequestDTO;
import com.api.dto.response.PerfilResponseDTO;
import com.api.exceptions.ProfileAlreadyAssignedException;
import com.api.exceptions.ProfileNotFoundException;
import com.api.exceptions.UserNotFoundException;
import com.api.mapper.PerfilMapper;
import com.api.repositories.PerfilRepository;
import com.api.repositories.UsuarioRepository;
import com.api.services.PerfilService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfilServiceImpl implements PerfilService {

    final private PerfilRepository perfilRepository;
    final private UsuarioRepository usuarioRepository;
    final private PerfilMapper perfilMapper;

    public PerfilServiceImpl(PerfilRepository perfilRepository, UsuarioRepository usuarioRepository, PerfilMapper perfilMapper) {
        this.perfilRepository = perfilRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilMapper = perfilMapper;
    }

    @Override
    public PerfilResponseDTO crearPerfil(PerfilRequestDTO perfilRequestDTO) {

        Usuario usuario = usuarioRepository.findById(perfilRequestDTO.idUsuario())
                .orElseThrow(() -> new UserNotFoundException(perfilRequestDTO.idUsuario()));

        if (usuario.getPerfil() != null) {
            throw new ProfileAlreadyAssignedException(usuario.getId());
        }

        Perfil perfil = perfilMapper.toEntity(perfilRequestDTO, usuario);
        Perfil perfilNuevo = perfilRepository.save(perfil);
        usuario.setPerfil(perfil);

        return perfilMapper.toResponse(perfilNuevo);
    }

    @Override
    public List<PerfilResponseDTO> obtenerPerfiles() {
        List<Perfil> perfiles = perfilRepository.findAll();
        return perfiles.stream()
                .map(perfilMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PerfilResponseDTO obtenerPerfilPorIdDeUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException(idUsuario));

        Perfil perfil = usuario.getPerfil();

        if (perfil == null) {
            throw new ProfileNotFoundException(idUsuario);
        }

        return perfilMapper.toResponse(perfil);
    }

    @Override
    public PerfilResponseDTO actualizarPerfil(Long idUsuario, PerfilRequestDTO perfilRequestDTO) {

        if (perfilRequestDTO.idUsuario() != null && !perfilRequestDTO.idUsuario().equals(idUsuario)) {
            throw new IllegalArgumentException("El idUsuario de la URL (" + idUsuario
                    + ") no coincide con el idUsuario enviado en el cuerpo (" + perfilRequestDTO.idUsuario() + ")");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException(idUsuario));

        Perfil perfilExistente = usuario.getPerfil();
        if (perfilExistente == null) {
            throw new ProfileNotFoundException(idUsuario);
        }

        if (perfilRequestDTO.nombre() != null && !perfilRequestDTO.nombre().isBlank()) {
            perfilExistente.setNombre(perfilRequestDTO.nombre().trim());
        }
        if (perfilRequestDTO.apellido() != null && !perfilRequestDTO.apellido().isBlank()) {
            perfilExistente.setApellido(perfilRequestDTO.apellido().trim());
        }
        if (perfilRequestDTO.telefono() != null && !perfilRequestDTO.telefono().isBlank()) {
            perfilExistente.setTelefono(perfilRequestDTO.telefono().trim());
        }

        Perfil perfilActualizado = perfilRepository.save(perfilExistente);
        return perfilMapper.toResponse(perfilActualizado);
    }
}
