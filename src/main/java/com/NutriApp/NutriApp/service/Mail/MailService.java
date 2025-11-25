package com.NutriApp.NutriApp.service.Mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Async
    public void enviarMail (String para, String asunto, String mensaje){
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setTo(para);
        mail.setSubject(asunto);
        mail.setText(mensaje);

        javaMailSender.send(mail);
    }
}
