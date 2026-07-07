package com.bookloop.shared.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Envio de e-mail agnóstico de provedor (SMTP). Nunca propaga falhas: se o envio
 * quebrar (ou o mail estiver desligado/sem configuração), apenas registra e segue,
 * para não afetar o fluxo que originou o e-mail.
 */
@Slf4j
@Service
public class MailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String from;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${bookloop.mail.enabled:false}") boolean enabled,
                       @Value("${bookloop.mail.from:BookLoop <no-reply@bookloop.app>}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.from = from;
    }

    public void sendHtml(String to, String subject, String html) {
        if (!enabled) {
            log.debug("Envio de e-mail desligado (bookloop.mail.enabled=false); destino={}", to);
            return;
        }
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("bookloop.mail.enabled=true, mas nenhum JavaMailSender configurado (defina spring.mail.host).");
            return;
        }
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            sender.send(message);
            log.info("E-mail enviado: assunto=\"{}\" destino={}", subject, to);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {}", to, e.getMessage());
        }
    }
}
