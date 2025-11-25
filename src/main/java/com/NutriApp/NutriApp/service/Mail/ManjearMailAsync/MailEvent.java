package com.NutriApp.NutriApp.service.Mail.ManjearMailAsync;

public class MailEvent {
    private final String emailUsuario;
    private final String asunto;
    private final String mensaje;

    public MailEvent(String emailUsuario, String asunto, String mensaje) {
        this.emailUsuario = emailUsuario;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    public String getEmailUsuario() { return emailUsuario; }
    public String getAsunto() { return asunto; }
    public String getMensaje() { return mensaje; }
}
