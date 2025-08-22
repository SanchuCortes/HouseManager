package com.example.housemanager.database.pojo;

/**
 * Fila de clasificación por liga: usuario y puntos totales.
 * Room puede mapearla por nombre de columnas de la query (userId, totalPoints).
 */
public class ManagerScoreRow {
    public long userId;
    public int totalPoints;

    public ManagerScoreRow() {}

    public long getUserId() { return userId; }
    public int getTotalPoints() { return totalPoints; }
}
