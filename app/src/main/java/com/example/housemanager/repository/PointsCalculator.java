package com.example.housemanager.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.housemanager.database.entities.LineupEntryEntity;
import com.example.housemanager.database.entities.MatchEntity;
import com.example.housemanager.database.entities.MatchEventEntity;

import java.util.List;

/**
 * Calculadora de puntos por jugador en UN partido, según reglas solicitadas.
 * No inventa datos: si faltan eventos/alineaciones, se asume 0 para esas partes.
 */
public final class PointsCalculator {

    /**
     * Devuelve los puntos por gol según la posición (en español).
     * - Delantero: 4
     * - Medio:     5
     * - Defensa:   6
     * - Portero:   6
     */
    public int pointsForPositionGoal(@Nullable String position) {
        if (position == null) return 4; // valor por defecto conservador
        switch (position) {
            case "Delantero": return 4;
            case "Medio":     return 5;
            case "Defensa":   return 6;
            case "Portero":   return 6;
            default:            return 4;
        }
    }

    /** Determina el teamId del jugador en este partido a partir de la alineación, si es posible. */
    private int resolveTeamIdFromLineup(int fallbackTeamId, int playerId, @Nullable List<LineupEntryEntity> lineup) {
        if (lineup != null) {
            for (LineupEntryEntity le : lineup) {
                if (le != null && le.getPlayerId() == playerId) {
                    return le.getTeamId();
                }
            }
        }
        return fallbackTeamId;
    }

    /** Cuenta si el jugador tiene algún evento personal en el partido. */
    private boolean hasAnyPersonalEvent(int playerId, @Nullable List<MatchEventEntity> events) {
        if (events == null) return false;
        for (MatchEventEntity e : events) {
            if (e != null && e.getPlayerId() == playerId) return true;
        }
        return false;
    }

    /**
     * Calcula los puntos de un jugador en un partido concreto aplicando las reglas proporcionadas.
     * No asume datos si faltan: suma 0 en esos apartados.
     */
    public int computeForPlayerInMatch(
            int playerId,
            @Nullable String playerPosition, // "Portero","Defensa","Medio","Delantero"
            int playerTeamId,
            @NonNull MatchEntity match,
            @Nullable List<MatchEventEntity> events,
            @Nullable List<LineupEntryEntity> lineup // entradas de ese match (STARTER/SUB)
    ) {
        int pts = 0;

        // 0) Determinar teamId real para este partido en base a la alineación
        int matchTeamId = resolveTeamIdFromLineup(playerTeamId, playerId, lineup);

        // 1) Goles (REGULAR/PENALTY suman; OWN no suma)
        int goals = 0;
        int assists = 0;
        int yellows = 0;
        boolean hasSecondYellow = false;
        boolean hasDirectRed = false;
        if (events != null) {
            for (MatchEventEntity e : events) {
                if (e == null || e.getPlayerId() != playerId) continue;
                String t = e.getType() != null ? e.getType().toUpperCase() : "";
                switch (t) {
                    case "GOAL":
                    case "GOAL_REGULAR":
                    case "PENALTY_GOAL":
                    case "GOAL_PENALTY":
                    case "PENALTY":
                        goals++;
                        break;
                    case "GOAL_OWN":
                    case "OWN_GOAL":
                        // no suma
                        break;
                    case "ASSIST":
                        assists++;
                        break;
                    case "CARD_YELLOW":
                    case "YELLOW":
                    case "YELLOW_CARD":
                        yellows++;
                        break;
                    case "CARD_SECOND_YELLOW":
                    case "SECOND_YELLOW_CARD":
                        hasSecondYellow = true;
                        break;
                    case "CARD_RED":
                    case "RED":
                    case "RED_CARD":
                        hasDirectRed = true;
                        break;
                }
            }
        }
        pts += goals * pointsForPositionGoal(playerPosition);
        pts += assists * 3;

        // 2) Titular/Suplente: SUB solo si jugó (proxy: tuvo evento)
        boolean isStarter = false;
        boolean isSub = false;
        if (lineup != null) {
            for (LineupEntryEntity le : lineup) {
                if (le != null && le.getPlayerId() == playerId) {
                    String role = le.getRole();
                    if (role != null && role.equalsIgnoreCase("STARTER")) { isStarter = true; break; }
                    if (role != null && role.equalsIgnoreCase("SUB")) { isSub = true; }
                }
            }
        }
        if (isStarter) {
            pts += 2;
        } else if (isSub && hasAnyPersonalEvent(playerId, events)) {
            pts += 1;
        }

        // 3) Tarjetas: YELLOW -1 por cada; SECOND_YELLOW -3 adicional (neto -4 con al menos una amarilla);
        //    RED directa -4 y NO acumula amarillas
        if (hasDirectRed) {
            pts += -4;
        } else {
            if (yellows > 0) pts += (-1 * yellows);
            if (hasSecondYellow) pts += -3; // adicional
        }

        // 4) Resultado del equipo: Victoria +3, Empate +1, Derrota 0 (si hay marcador)
        Integer hs = match.getHomeScore();
        Integer as = match.getAwayScore();
        if (hs != null && as != null) {
            boolean isHome = (matchTeamId == match.getHomeTeamId());
            int my = isHome ? hs : as;
            int opp = isHome ? as : hs;
            if (my > opp) pts += 3; else if (my == opp) pts += 1; // derrota 0
        }

        return pts;
    }
}
