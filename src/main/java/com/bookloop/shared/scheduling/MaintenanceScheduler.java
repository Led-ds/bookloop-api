package com.bookloop.shared.scheduling;

import com.bookloop.rental.application.RentalService;
import com.bookloop.reservation.application.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Vigias por tempo (item 4b). Rodam periodicamente (1 instância fixa no App Runner).
 * Cada varredura é isolada num try/catch para que uma falha não derrube o agendador.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceScheduler {

    private final ReservationService reservationService;
    private final RentalService rentalService;

    /** Expira ofertas de reserva vencidas (48h) e passa a vez ao próximo. */
    @Scheduled(
            fixedDelayString = "${bookloop.maintenance.sweep-ms:900000}",
            initialDelayString = "${bookloop.maintenance.initial-ms:60000}")
    public void sweepExpiredOffers() {
        try {
            reservationService.expireStaleOffers();
        } catch (Exception e) {
            log.error("Falha ao expirar ofertas de reserva: {}", e.getMessage(), e);
        }
    }

    /** Encerra solicitações PENDING não respondidas pelo dono (48h) e avisa os envolvidos. */
    @Scheduled(
            fixedDelayString = "${bookloop.maintenance.sweep-ms:900000}",
            initialDelayString = "${bookloop.maintenance.initial-ms:60000}")
    public void sweepStalePendingRequests() {
        try {
            rentalService.expireStalePendingRequests();
        } catch (Exception e) {
            log.error("Falha ao expirar solicitações pendentes: {}", e.getMessage(), e);
        }
    }
}
