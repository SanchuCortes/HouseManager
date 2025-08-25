package com.example.housemanager.model.mappers;

import android.util.Log;

import com.example.housemanager.api.models.MatchAPI;
import com.example.housemanager.api.models.PlayerAPI;
import com.example.housemanager.api.models.ScoreAPI;
import com.example.housemanager.api.models.TeamAPI;
import com.example.housemanager.database.entities.MatchEntity;
import com.example.housemanager.database.entities.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper de objetos de API a entidades de Room (y utilidades afines).
 * Extraído desde FootballRepository sin cambiar la lógica.
 */
public final class ApiToEntityMapper {
    private static final String TAG = "ApiToEntityMapper";

    private ApiToEntityMapper() {}

    public static List<PlayerEntity> convertApiPlayersToEntities(List<PlayerAPI> apiPlayers, TeamAPI team) {
        List<PlayerEntity> out = new ArrayList<>();
        long now = System.currentTimeMillis();
        if (apiPlayers == null || team == null) return out;
        for (PlayerAPI p : apiPlayers) {
            PlayerEntity e = new PlayerEntity();
            e.setPlayerId(p.getId());
            e.setName(p.getName() != null ? p.getName() : "Jugador");
            e.setTeamId(team.getId());
            e.setTeamName(team.getName() != null ? team.getName() : "");
            e.setPosition(translatePositionToSpanish(p.getPosition()));
            e.setNationality(p.getNationality() != null ? p.getNationality() : "España");
            e.setCurrentPrice(calculatePlayerPrice(e.getPosition()));
            // No tocar totalPoints aquí
            e.setAvailable(true);
            e.setUpdatedAt(now);
            out.add(e);
        }
        return out;
    }

    public static List<MatchEntity> convertApiMatchesToEntities(List<MatchAPI> apiMatches) {
        List<MatchEntity> out = new ArrayList<>();
        if (apiMatches == null) return out;
        for (MatchAPI m : apiMatches) {
            try {
                MatchEntity e = new MatchEntity();
                e.setMatchId(m.getId());
                if (m.getHomeTeam() != null) {
                    e.setHomeTeamId(m.getHomeTeam().getId());
                    e.setHomeTeamName(m.getHomeTeam().getName());
                }
                if (m.getAwayTeam() != null) {
                    e.setAwayTeamId(m.getAwayTeam().getId());
                    e.setAwayTeamName(m.getAwayTeam().getName());
                }
                String utc = m.getUtcDate();
                long millis = 0L;
                if (utc != null && !utc.isEmpty()) {
                    try {
                        java.time.Instant ins = java.time.OffsetDateTime.parse(utc).toInstant();
                        millis = ins.toEpochMilli();
                    } catch (Exception exParse) {
                        // Fallback a parser robusto
                        millis = parseIsoToMillis(utc);
                        if (millis <= 0L) {
                            Log.w(TAG, "utcDate inválido: " + utc, exParse);
                            millis = 0L;
                        }
                    }
                }
                e.setUtcDateMillis(millis);
                e.setStatus(m.getStatus());
                Integer home = null;
                Integer away = null;
                ScoreAPI score = m.getScore();
                if (score != null && score.getFullTime() != null) {
                    home = score.getFullTime().getHome();
                    away = score.getFullTime().getAway();
                }
                e.setHomeScore(home);
                e.setAwayScore(away);
                out.add(e);
            } catch (Exception ex) {
                Log.w(TAG, "Error convirtiendo partido id=" + (m != null ? m.getId() : -1), ex);
            }
        }
        return out;
    }

    public static String translatePositionToSpanish(String englishPosition) {
        if (englishPosition == null) return "Medio";
        String pos = englishPosition.toUpperCase();
        if (pos.contains("GOALKEEPER")) return "Portero";
        if (pos.contains("DEFENDER")) return "Defensa";
        if (pos.contains("MIDFIELDER")) return "Medio";
        if (pos.contains("FORWARD") || pos.contains("ATTACKER") || pos.contains("STRIKER")) return "Delantero";
        return "Medio";
    }

    public static int calculatePlayerPrice(String position) {
        if (position == null) return 12;
        switch (position) {
            case "Portero":   return (int) (Math.random() * 10) + 8;
            case "Defensa":   return (int) (Math.random() * 15) + 5;
            case "Medio":     return (int) (Math.random() * 20) + 8;
            case "Delantero": return (int) (Math.random() * 25) + 10;
            default:           return 12;
        }
    }

    public static long parseIsoToMillis(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;
        try {
            return java.time.Instant.parse(iso).toEpochMilli();
        } catch (Exception e1) {
            try {
                return java.time.OffsetDateTime.parse(iso, java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .toInstant().toEpochMilli();
            } catch (Exception e2) {
                try {
                    java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(iso,
                            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
                    return ldt.toInstant(java.time.ZoneOffset.UTC).toEpochMilli();
                } catch (Exception e3) {
                    return 0L;
                }
            }
        }
    }
}
