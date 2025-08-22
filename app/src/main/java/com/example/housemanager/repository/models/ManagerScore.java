package com.example.housemanager.repository.models;

/** Modelo simple para exponer la clasificación por usuario. */
public class ManagerScore {
    private long userId;
    private int totalPoints;

    public ManagerScore() {}
    public ManagerScore(long userId, int totalPoints) {
        this.userId = userId;
        this.totalPoints = totalPoints;
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
}
