package com.bookloop.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita tarefas agendadas. O ambiente roda com 1 instância fixa no App Runner,
 * então não há risco de execução duplicada (sem necessidade de ShedLock por ora).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
