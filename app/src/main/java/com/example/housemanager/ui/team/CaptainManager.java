package com.example.housemanager.ui.team;

import android.content.Context;
import android.content.SharedPreferences;

public class CaptainManager {
    private static final String PREFS = "captain_prefs";

    // NUEVO: clave por liga y usuario
    private static String key(long leagueId, long userId) {
        return "captain_league_" + leagueId + "_user_" + userId;
    }

    // Nuevos métodos por leagueId + userId
    public static void setCaptain(Context ctx, long leagueId, long userId, int playerId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putInt(key(leagueId, userId), playerId).apply();
    }

    public static int getCaptain(Context ctx, long leagueId, long userId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getInt(key(leagueId, userId), -1);
    }

    // LEGACY: por teamId (no usar)
    @Deprecated
    public static void setCaptain(Context ctx, int teamId, int playerId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putInt("captain_team_" + teamId, playerId).apply();
    }

    @Deprecated
    public static int getCaptain(Context ctx, int teamId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getInt("captain_team_" + teamId, -1);
    }
}
