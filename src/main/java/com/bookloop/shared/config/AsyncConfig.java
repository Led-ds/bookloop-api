package com.bookloop.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Habilita @Async (envio de e-mail fora da transação/thread do request). */
@Configuration
@EnableAsync
public class AsyncConfig {
}
