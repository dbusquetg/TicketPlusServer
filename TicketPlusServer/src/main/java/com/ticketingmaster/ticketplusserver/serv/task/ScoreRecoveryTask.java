package com.ticketingmaster.ticketplusserver.serv.task;

import com.ticketingmaster.ticketplusserver.model.Role;
import com.ticketingmaster.ticketplusserver.model.User;
import com.ticketingmaster.ticketplusserver.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tarea programada que recupera puntos a los usuarios USER activos.
 *
 * Se ejecuta cada 24 horas y suma 10 puntos al score de cada usuario USER activo,
 * sin superar el valor máximo de 100.
 *
 * No afecta a los usuarios ADMIN ni a los desactivados.
 * @author David Busquet
 */
@Component
public class ScoreRecoveryTask {

    private static final Logger log = LoggerFactory.getLogger(ScoreRecoveryTask.class);

    // Puntos sumados a cada usuario USER en cada ejecución del job.
    private static final int PUNTOS_RECUPERACION = 10;

    // Valor máximo del score que un usuario puede tener. */
    private static final int SCORE_MAXIMO = 100;

    // Periodo de ejecución del job: 24 horas en milisegundos. */
    private static final long INTERVALO_24H_MS = 24L * 60L * 60L * 1000L;

    private final UserRepo userRepo;

    public ScoreRecoveryTask(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * Ejecuta cada 24 horas la recuperación de puntos.
     *
     * Para cada usuario USER activo:
     *   nuevoScore = min(scoreActual + 10, 100)
     *
     * Si el usuario ya tiene 100 puntos, se queda igual.
     */
    @Scheduled(fixedRate = INTERVALO_24H_MS)
    @Transactional
    public void recuperarPuntos() {
        List<User> usuariosActivos =
                userRepo.findByRoleAndActiveTrueOrderByUsernameAsc(Role.USER);

        int actualizados = 0;
        for (User user : usuariosActivos) {
            int scoreActual = user.getScore();
            if (scoreActual < SCORE_MAXIMO) {
                int nuevoScore = Math.min(SCORE_MAXIMO, scoreActual + PUNTOS_RECUPERACION);
                user.setScore(nuevoScore);
                actualizados++;
            }
        }

        if (actualizados > 0) {
            userRepo.saveAll(usuariosActivos);
            log.info("ScoreRecoveryTask — {} usuarios USER recuperaron puntos (+{} hasta máximo {})",
                    actualizados, PUNTOS_RECUPERACION, SCORE_MAXIMO);
        } else {
            log.info("ScoreRecoveryTask — sin usuarios para actualizar");
        }
    }
}