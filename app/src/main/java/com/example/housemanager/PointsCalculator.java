package com.yourpackage.housemanager.utils;

import android.util.Log;
import java.util.List;

public class PointsCalculator {
    private static final String TAG = "PointsCalculator";

    // MÉTODO PRINCIPAL DE CÁLCULO
    public static int calculatePlayerPoints(PlayerMatchStats stats, String position, MatchData match) {
        int points = 0;
        String logDetails = "🏆 " + stats.getPlayerName() + " (" + position + "): ";

        try {
            // 1. PARTICIPACIÓN (4 pts titular, 2 pts suplente)
            int participationPoints = getParticipationPoints(stats, match);
            points += participationPoints;
            logDetails += participationPoints > 0 ? "Participación +" + participationPoints + ", " : "";

            // 2. GOLES según posición (10/8/6/4 pts)
            int goalPoints = stats.getGoals() * getGoalPoints(position);
            points += goalPoints;
            if (goalPoints > 0) logDetails += "Goles +" + goalPoints + ", ";

            // 3. ASISTENCIAS (4 pts cada una)
            int assistPoints = stats.getAssists() * 4;
            points += assistPoints;
            if (assistPoints > 0) logDetails += "Asistencias +" + assistPoints + ", ";

            // 4. CLEAN SHEET / GOLES RECIBIDOS (diferente por posición)
            int cleanSheetPoints = getGoalsConcededPoints(position, stats.getTeamGoalsAgainst());
            points += cleanSheetPoints;
            if (cleanSheetPoints != 0) logDetails += "CleanSheet " + (cleanSheetPoints > 0 ? "+" : "") + cleanSheetPoints + ", ";

            // 5. PENALTIES (+3 marcado, -2 fallado)
            int penaltyPoints = stats.getPenaltiesScored() * 3 - stats.getPenaltiesMissed() * 2;
            points += penaltyPoints;
            if (penaltyPoints != 0) logDetails += "Penalties " + (penaltyPoints > 0 ? "+" : "") + penaltyPoints + ", ";

            // 6. TARJETAS (-1 amarilla, -3 roja)
            int cardPoints = -(stats.getYellowCards() * 1 + stats.getRedCards() * 3);
            points += cardPoints;
            if (cardPoints < 0) logDetails += "Tarjetas " + cardPoints + ", ";

            // 7. BONUS POR ESTADÍSTICAS DE EQUIPO
            int bonusPoints = calculateTeamBonus(stats, position, match);
            points += bonusPoints;
            if (bonusPoints != 0) logDetails += "Bonus " + (bonusPoints > 0 ? "+" : "") + bonusPoints + ", ";

            // 8. MULTIPLICADOR DE CAPITÁN (x2)
            if (stats.isCaptain()) {
                int originalPoints = points;
                points = points * 2;
                logDetails += "CAPITÁN x2 (era " + originalPoints + "), ";
            }

            // 9. BONUS POR VICTORIA (+1 punto adicional)
            if (stats.isTeamWon()) {
                points += 1;
                logDetails += "Victoria +1, ";
            }

            // Mínimo 0 puntos
            points = Math.max(0, points);

            logDetails += "TOTAL: " + points + " pts";
            Log.d(TAG, logDetails);

        } catch (Exception e) {
            Log.e(TAG, "Error calculando puntos para " + stats.getPlayerName(), e);
            return 0;
        }

        return points;
    }

    // PARTICIPACIÓN EN EL PARTIDO
    private static int getParticipationPoints(PlayerMatchStats stats, MatchData match) {
        if (isPlayerInLineup(stats.getPlayerId(), match.getLineup())) {
            return 4; // Titular
        } else if (isPlayerSubstitute(stats.getPlayerId(), match.getSubstitutions())) {
            return 2; // Suplente que entra
        }
        return 0; // No juega
    }

    private static boolean isPlayerInLineup(int playerId, List<LineupPlayer> lineup) {
        if (lineup == null) return false;
        for (LineupPlayer player : lineup) {
            if (player.getPlayerId() == playerId) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPlayerSubstitute(int playerId, List<Substitution> substitutions) {
        if (substitutions == null) return false;
        for (Substitution sub : substitutions) {
            if (sub.getPlayerInId() == playerId) {
                return true;
            }
        }
        return false;
    }

    // PUNTOS POR GOLES SEGÚN POSICIÓN
    private static int getGoalPoints(String position) {
        switch (position.toUpperCase()) {
            case "GK": return 10;  // Portero
            case "DEF": return 8;  // Defensa
            case "MID": return 6;  // Centrocampista
            case "FWD": return 4;  // Delantero
            default: return 5;     // Posición desconocida
        }
    }

    // CLEAN SHEET Y GOLES RECIBIDOS
    private static int getGoalsConcededPoints(String position, int goalsAgainst) {
        switch (position.toUpperCase()) {
            case "GK": // Porteros
                if (goalsAgainst == 0) return 6;      // 0 goles = +6pts
                else if (goalsAgainst == 1) return 4; // 1 gol = +4pts
                else if (goalsAgainst == 2) return 2; // 2 goles = +2pts
                else if (goalsAgainst == 3) return 0; // 3 goles = 0pts
                else return -(goalsAgainst - 3);      // 4+ goles = -1pt por gol extra

            case "DEF": // Defensas
                if (goalsAgainst == 0) return 4;      // 0 goles = +4pts
                else if (goalsAgainst == 1) return 2; // 1 gol = +2pts
                else if (goalsAgainst == 2) return 1; // 2 goles = +1pt
                else if (goalsAgainst == 3) return 0; // 3 goles = 0pts
                else return -((goalsAgainst - 3) / 2); // 4+ goles = -1pt cada 2 goles

            case "MID": // Centrocampistas
                if (goalsAgainst == 0) return 2;      // 0 goles = +2pts
                else if (goalsAgainst <= 2) return 1; // 1-2 goles = +1pt
                else return 0;                        // 3+ goles = 0pts

            case "FWD": // Delanteros
                if (goalsAgainst == 0) return 1;      // 0 goles = +1pt
                else return 0;                        // 1+ goles = 0pts

            default:
                return 0;
        }
    }

    // BONUS POR ESTADÍSTICAS DE EQUIPO
    private static int calculateTeamBonus(PlayerMatchStats stats, String position, MatchData match) {
        int bonus = 0;
        TeamStatistics teamStats = match.getTeamStatistics(stats.getTeamId());

        if (teamStats == null) return 0;

        try {
            switch (position.toUpperCase()) {
                case "GK": // Porteros: Bonus por paradas
                    int saves = teamStats.getSaves();
                    bonus += saves / 3; // +1 punto cada 3 paradas
                    break;

                case "MID": // Centrocampistas: Bonus por posesión
                    int possession = teamStats.getBallPossession();
                    if (possession >= 65) bonus += 2;
                    else if (possession >= 60) bonus += 1;
                    break;

                case "FWD": // Delanteros: Bonus por disparos
                    int shots = teamStats.getShotsOnGoal();
                    if (shots >= 6) bonus += 2;
                    else if (shots >= 4) bonus += 1;
                    break;

                case "DEF": // Defensas: Sin bonus específico adicional
                default:
                    break;
            }

            // PENALIZACIÓN POR FALTAS (para todas las posiciones)
            int fouls = teamStats.getFouls();
            if (fouls >= 20) bonus -= 2;
            else if (fouls >= 15) bonus -= 1;

        } catch (Exception e) {
            Log.w(TAG, "Error calculando bonus de equipo", e);
        }

        return bonus;
    }

    // MÉTODO PARA CALCULAR PUNTOS DE MÚLTIPLES JUGADORES
    public static void calculateTeamPoints(List<PlayerMatchStats> players, MatchData match, TeamPointsCallback callback) {
        int totalPoints = 0;
        StringBuilder summary = new StringBuilder("📊 Resumen de puntuaciones:\n");

        for (PlayerMatchStats player : players) {
            int points = calculatePlayerPoints(player, player.getPosition(), match);
            player.setMatchPoints(points);
            totalPoints += points;

            summary.append(String.format("• %s: %d pts\n", player.getPlayerName(), points));
        }

        summary.append(String.format("\n🏆 TOTAL EQUIPO: %d puntos", totalPoints));

        if (callback != null) {
            callback.onPointsCalculated(totalPoints, summary.toString());
        }
    }

    public interface TeamPointsCallback {
        void onPointsCalculated(int totalPoints, String summary);
    }

    // SIMULACIÓN PARA TESTING (datos mock de ejemplo)
    public static PlayerMatchStats createMockPlayerStats(String playerName, String position, int teamId) {
        PlayerMatchStats stats = new PlayerMatchStats();
        stats.setPlayerName(playerName);
        stats.setPosition(position);
        stats.setTeamId(teamId);

        // Estadísticas simuladas realistas
        switch (position.toUpperCase()) {
            case "GK":
                stats.setGoals(0);
                stats.setAssists(0);
                stats.setPenaltiesScored(0);
                stats.setPenaltiesMissed(0);
                stats.setYellowCards(0);
                stats.setRedCards(0);
                stats.setTeamGoalsAgainst(1); // 1 gol recibido
                break;

            case "DEF":
                stats.setGoals(0);
                stats.setAssists(1);
                stats.setPenaltiesScored(0);
                stats.setPenaltiesMissed(0);
                stats.setYellowCards(1);
                stats.setRedCards(0);
                stats.setTeamGoalsAgainst(1);
                break;

            case "MID":
                stats.setGoals(1);
                stats.setAssists(1);
                stats.setPenaltiesScored(0);
                stats.setPenaltiesMissed(0);
                stats.setYellowCards(0);
                stats.setRedCards(0);
                stats.setTeamGoalsAgainst(1);
                break;

            case "FWD":
                stats.setGoals(2);
                stats.setAssists(0);
                stats.setPenaltiesScored(1);
                stats.setPenaltiesMissed(0);
                stats.setYellowCards(0);
                stats.setRedCards(0);
                stats.setTeamGoalsAgainst(1);
                break;
        }

        stats.setCaptain(false);
        stats.setTeamWon(true);

        return stats;
    }
}

// CLASES DE DATOS PARA ESTADÍSTICAS
class PlayerMatchStats {
    private String playerName;
    private String position;
    private int teamId;
    private int goals;
    private int assists;
    private int penaltiesScored;
    private int penaltiesMissed;
    private int yellowCards;
    private int redCards;
    private int teamGoalsAgainst;
    private boolean isCaptain;
    private boolean isTeamWon;
    private int matchPoints;

    // Getters y Setters
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getGoals() { return goals; }
    public void setGoals(int goals) { this.goals = goals; }

    public int getAssists() { return assists; }
    public void setAssists(int assists) { this.assists = assists; }

    public int getPenaltiesScored() { return penaltiesScored; }
    public void setPenaltiesScored(int penaltiesScored) { this.penaltiesScored = penaltiesScored; }

    public int getPenaltiesMissed() { return penaltiesMissed; }
    public void setPenaltiesMissed(int penaltiesMissed) { this.penaltiesMissed = penaltiesMissed; }

    public int getYellowCards() { return yellowCards; }
    public void setYellowCards(int yellowCards) { this.yellowCards = yellowCards; }

    public int getRedCards() { return redCards; }
    public void setRedCards(int redCards) { this.redCards = redCards; }

    public int getTeamGoalsAgainst() { return teamGoalsAgainst; }
    public void setTeamGoalsAgainst(int teamGoalsAgainst) { this.teamGoalsAgainst = teamGoalsAgainst; }

    public boolean isCaptain() { return isCaptain; }
    public void setCaptain(boolean captain) { isCaptain = captain; }

    public boolean isTeamWon() { return isTeamWon; }
    public void setTeamWon(boolean teamWon) { isTeamWon = teamWon; }

    public int getMatchPoints() { return matchPoints; }
    public void setMatchPoints(int matchPoints) { this.matchPoints = matchPoints; }

    public int getPlayerId() { return teamId * 100 + playerName.hashCode() % 100; } // Mock ID
}

class MatchData {
    private List<LineupPlayer> lineup;
    private List<Substitution> substitutions;
    private List<TeamStatistics> teamStatistics;

    public List<LineupPlayer> getLineup() { return lineup; }
    public void setLineup(List<LineupPlayer> lineup) { this.lineup = lineup; }

    public List<Substitution> getSubstitutions() { return substitutions; }
    public void setSubstitutions(List<Substitution> substitutions) { this.substitutions = substitutions; }

    public TeamStatistics getTeamStatistics(int teamId) {
        if (teamStatistics == null) return null;
        for (TeamStatistics stats : teamStatistics) {
            if (stats.getTeamId() == teamId) {
                return stats;
            }
        }
        return null;
    }

    public void setTeamStatistics(List<TeamStatistics> teamStatistics) {
        this.teamStatistics = teamStatistics;
    }
}

class LineupPlayer {
    private int playerId;
    private String playerName;

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}

class Substitution {
    private int playerOutId;
    private int playerInId;
    private int minute;

    public int getPlayerOutId() { return playerOutId; }
    public void setPlayerOutId(int playerOutId) { this.playerOutId = playerOutId; }
    public int getPlayerInId() { return playerInId; }
    public void setPlayerInId(int playerInId) { this.playerInId = playerInId; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
}

class TeamStatistics {
    private int teamId;
    private int ballPossession;
    private int saves;
    private int shotsOnGoal;
    private int fouls;

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }
    public int getBallPossession() { return ballPossession; }
    public void setBallPossession(int ballPossession) { this.ballPossession = ballPossession; }
    public int getSaves() { return saves; }
    public void setSaves(int saves) { this.saves = saves; }
    public int getShotsOnGoal() { return shotsOnGoal; }
    public void setShotsOnGoal(int shotsOnGoal) { this.shotsOnGoal = shotsOnGoal; }
    public int getFouls() { return fouls; }
    public void setFouls(int fouls) { this.fouls = fouls; }
}