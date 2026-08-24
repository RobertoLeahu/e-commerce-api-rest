package com.api.services.impl;

import com.api.domain.enums.Rol;
import com.api.domain.models.Perfil;
import com.api.domain.models.Usuario;
import com.api.dto.request.LoginRequestDTO;
import com.api.dto.request.RegisterRequestDTO;
import com.api.dto.response.AuthResponseDTO;
import com.api.exceptions.UserNotFoundException;
import com.api.repositories.UsuarioRepository;
import com.api.security.SecurityUser;
import com.api.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    final private UsuarioRepository usuarioRepository;
    final private PasswordEncoder passwordEncoder;
    final private JwtService jwtService;
    final private EmailService emailService;
    final private AuthenticationManager authenticationManager;

    @Transactional
    public String register(RegisterRequestDTO request){
        // 1. Validar que la request si existe ya en la BDD
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email introducido ya existe");
        }

        // 2. Instanciar la request de Usuario
        Usuario usuario = Usuario.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .rol(Rol.CLIENTE)
                .isEnable(false)
                .build();

        // 3. Relacionar la instancia de Usuario a un Perfil
        Perfil perfil = Perfil.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .telefono(request.telefono())
                .build();

        usuario.setPerfil(perfil);

        // 4. Persistir Usuario y por CascadeType.ALL se guarda también Perfil
        usuarioRepository.save(usuario);

        // 5. Generar token de verificación stateless y enviar correo asíncrono
        String verificationCode = jwtService.generateVerificationToken(usuario.getEmail());
        emailService.sendVerificationEmail(usuario.getEmail(), verificationCode);

        return "Registro exitoso. Revisa tu correo electrónico para activar tu cuenta.";
    }

    @Transactional
    public String confirmAccount(String token){

        // 1. Validar si el token de confirmación es válido
        if (!jwtService.isVerificationTokenValid(token)) {
            throw new IllegalArgumentException("El token de verificación es inválido o ha expirado.");
        }

        // 2. Extraer email del token y crear instancia
        String email = jwtService.extractUsername(token);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));

        // 3. Validar si isEnable esta activado o no
        if (usuario.isEnable()) {
            return "La cuenta ya se encuentra activada previamente.";
        }

        // 4. Activar enable y persistir en la BDD
        usuario.setEnable(true);
        usuarioRepository.save(usuario);

        return "¡Cuenta activada con éxito! Ya puedes iniciar sesión.";
    }

    public AuthResponseDTO login(LoginRequestDTO request){
        // 1. Autentificar credenciales de la request y valida si la cuenta está activa/bloqueada
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Extraer principal
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

        // 3. Generar token y retornar DTO (usando el constructor con 'Bearer' por defecto)
        String accesToken = jwtService.generateAccessToken(securityUser);

        return new AuthResponseDTO(
                accesToken,
                securityUser.getUsername(),
                securityUser.getUsuario().getRol()
        );
    }
}
