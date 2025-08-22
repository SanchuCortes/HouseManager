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

        // 1) Goles (GOAL y PENALTY_GOAL/PENALTY suman; OWN_GOAL no suma)
        int goals = 0;
        if (events != null) {
            for (MatchEventEntity e : events) {
                if (e != null && e.getPlayerId() == playerId) {
                    String type = e.getType();
                    if (type == null) continue;
                    String t = type.toUpperCase();
                    if (t.equals("GOAL") || t.equals("PENALTY_GOAL") || t.equals("PENALTY")) {
                        goals++;
                    } else if (t.equals("OWN_GOAL")) {
                        // no suma puntos por gol en propia puerta
                    }
                }
            }
        }
        pts += goals * pointsForPositionGoal(playerPosition);

        // 2) Titular/Suplente
        boolean isStarter = false;
        boolean isSub = false;
        if (lineup != null) {
            for (LineupEntryEntity le : lineup) {
                if (le != null && le.getPlayerId() == playerId) {
                    String role = le.getRole();
                    if (role != null) {
                        if (role.equalsIgnoreCase("STARTER")) {
                            isStarter = true;
                            break; // STARTER tiene prioridad
                        } else if (role.equalsIgnoreCase("SUB")) {
                            isSub = true; // si no encontramos STARTER, SUB vale 1 punto
                        }
                    }
                }
            }
        }
        if (isStarter) {
            pts += 2;
        } else if (isSub) {
            pts += 1;
        }

        // 3) Tarjetas: amarilla -1, roja -3 (cap: si hay expulsión, no acumular -1 adicional)
        int yellow = 0;
        boolean hasRedExpulsion = false; // RED_CARD o SECOND_YELLOW_CARD
        if (events != null) {
            for (MatchEventEntity e : events) {
                if (e != null && e.getPlayerId() == playerId) {
                    String type = e.getType();
                    if (type == null) continue;
                    String t = type.toUpperCase();
                    if (t.equals("YELLOW") || t.equals("YELLOW_CARD")) {
                        yellow++;
                    } else if (t.equals("RED") || t.equals("RED_CARD") || t.equals("SECOND_YELLOW_CARD")) {
                        hasRedExpulsion = true;
                    }
                }
            }
        }
        if (hasRedExpulsion) {
            pts += -3; // tope por expulsión
        } else if (yellow > 0) {
            pts += (-1 * yellow);
        }

        // 4) Resultado del equipo: Victoria +3, Empate +1, Derrota 0 (si hay marcador)
        Integer hs = match.getHomeScore();
        Integer as = match.getAwayScore();
        if (hs != null && as != null) {
            boolean isHome = (playerTeamId == match.getHomeTeamId());
            int my = isHome ? hs : as;
            int opp = isHome ? as : hs;
            if (my > opp) pts += 3; else if (my == opp) pts += 1; // derrota suma 0
        }

        return pts;
    }
}
