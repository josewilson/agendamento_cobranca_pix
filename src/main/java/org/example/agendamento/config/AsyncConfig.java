package org.example.agendamento.config;

import org.example.agendamento.adapter.in.web.logging.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * Pool dedicado para os listeners de evento de dominio (NotificacaoEventListener,
 * CalendarioEventListener, EstornoEventListener) rodarem fora da thread da requisicao HTTP
 * que publicou o evento. Sem isso, ApplicationEventPublisher.publishEvent (sincrono por
 * padrao) faz o I/O de SMTP/Twilio/Google Calendar/gateway de pagamento bloquear a resposta
 * ao usuario — e, sob carga, uma API externa lenta esgota o pool de threads do Tomcat (achado
 * de auditoria de performance). Pool separado do HTTP do Tomcat de proposito: uma integracao
 * externa lenta nao deve competir por threads com o atendimento de outras requisicoes.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "eventosExecutor")
    public TaskExecutor eventosExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("evento-dominio-");
        // MDC e thread-local: sem copiar o requestId (CorrelationIdFilter) da thread da
        // requisicao HTTP pra thread do pool, os logs desses 3 listeners perderiam a
        // correlacao justamente no cenario onde ela mais importa (falha de notificacao/
        // calendario/estorno rodando em background).
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    private static final class MdcTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            Map<String, String> contextoDaRequisicao = MDC.getCopyOfContextMap();
            return () -> {
                if (contextoDaRequisicao != null) {
                    MDC.setContextMap(contextoDaRequisicao);
                }
                try {
                    runnable.run();
                } finally {
                    MDC.remove(CorrelationIdFilter.MDC_KEY);
                }
            };
        }
    }
}
