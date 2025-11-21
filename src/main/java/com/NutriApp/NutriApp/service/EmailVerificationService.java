package com.NutriApp.NutriApp.service;

import com.NutriApp.NutriApp.modelo.EmailVerificationCode;
import com.NutriApp.NutriApp.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EmailVerificationService {

    @Autowired
    private EmailVerificationRepository repository;

    @Autowired
    private JavaMailSender mailSender;

    // Generar código de 6 dígitos
    public String generarCodigo() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    // Guardar o actualizar código
    public void guardarCodigo(String email, String codigo) {

        EmailVerificationCode ver = repository.findByEmail(email)
                .orElse(new EmailVerificationCode());

        ver.setEmail(email);
        ver.setCodigo(codigo);
        ver.setExpiracion(LocalDateTime.now().plusMinutes(10));

        repository.save(ver);
    }

    // Enviar email con el código
    public void enviarCorreo(String email, String codigo) {

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Verificación de correo - NutriApp");
        mensaje.setText(
                "Tu código de verificación es: " + codigo +
                        "\n\nEste código expira en 10 minutos."
        );

        mailSender.send(mensaje);
    }

    // Verificar código ingresado por el usuario
    public boolean verificar(String email, String codigoIngresado) {

        Optional<EmailVerificationCode> registro = repository.findByEmail(email);

        if (registro.isEmpty())
            return false;

        EmailVerificationCode data = registro.get();

        // Expirado
        if (LocalDateTime.now().isAfter(data.getExpiracion()))
            return false;

        // Código incorrecto
        return data.getCodigo().equals(codigoIngresado);
    }

    public void eliminarCodigo(String email) {
        repository.findByEmail(email).ifPresent(repository::delete);
    }
}
