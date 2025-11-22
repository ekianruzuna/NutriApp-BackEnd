package com.NutriApp.NutriApp.service.Mail.ManjearMailAsync;

import com.NutriApp.NutriApp.service.Mail.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MailListener {
    @Autowired
    private MailService mailService;

    // Se ejecuta DESPUÉS de que la transacción commitée
    @Async
    @TransactionalEventListener
    public void mandarMailAsyncDespuesDeCommit(MailEvent event) {
        mailService.enviarMail(
                event.getEmailUsuario(),
                event.getAsunto(),
                event.getMensaje()
        );
    }
}
