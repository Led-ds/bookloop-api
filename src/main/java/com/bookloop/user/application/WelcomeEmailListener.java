package com.bookloop.user.application;

import com.bookloop.shared.mail.MailService;
import com.bookloop.user.domain.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * E-mail de boas-vindas. Dispara só após o cadastro ser efetivado (AFTER_COMMIT)
 * e em outra thread (@Async), para nunca bloquear nem reverter o registro.
 */
@Component
@RequiredArgsConstructor
public class WelcomeEmailListener {

    private final MailService mailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent e) {
        String html = """
                <div style="font-family:system-ui,Arial,sans-serif;max-width:520px;margin:auto">
                  <h2 style="color:#20462F">Bem-vindo(a) ao BookLoop, %s! 📚</h2>
                  <p>Que bom ter você na comunidade. Aqui os livros circulam de mão em mão:
                     você empresta os seus e pega emprestado os de outras pessoas.</p>
                  <p>Para começar, cadastre um livro na sua estante ou explore o acervo da comunidade.</p>
                  <p style="color:#6b7280;font-size:13px">Você está recebendo este e-mail porque criou uma conta no BookLoop.</p>
                </div>
                """.formatted(e.name());
        mailService.sendHtml(e.email(), "Bem-vindo(a) ao BookLoop!", html);
    }
}
