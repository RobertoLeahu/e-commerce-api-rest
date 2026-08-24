package com.api.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // Correcta
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${application.security.verification-url}")
    private String verificationBaseUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            String verificationUrl = verificationBaseUrl + "?token=" + token;

            helper.setTo(toEmail);
            helper.setSubject("Verifica tu cuenta de E-Commerce");
            helper.setFrom("no-reply@ecommerce.com");
            helper.setText(buildEmailBody(verificationUrl), true); // true habilita soporte HTML

            javaMailSender.send(mimeMessage);
            log.info("Correo de verificación enviado a: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Error al enviar el correo de verificación a {}: {}", toEmail, e.getMessage());
        }
    }

    private String buildEmailBody(String url) {
        return """
            <div style="font-family: Arial, sans-serif; padding: 20px;">
                <h2>¡Bienvenido a nuestro E-Commerce!</h2>
                <p>Por favor, haz clic en el siguiente enlace para verificar tu correo electrónico:</p>
                <a href="%s" style="background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;">
                    Verificar mi cuenta
                </a>
                <p style="margin-top: 15px; color: #666;">Este enlace expirará en 24 horas.</p>
            </div>
            """.formatted(url);
    }
}
