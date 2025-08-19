package com.example.housemanager;

import android.content.Context;
import android.content.SharedPreferences;

public class CaptainManager {
    private static final String PREFS = "captain_prefs";

    public static void setCaptain(Context ctx, int teamId, int playerId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putInt(key(teamId), playerId).apply();
    }

    public static int getCaptain(Context ctx, int teamId) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getInt(key(teamId), -1);
    }

    private static String key(int teamId) {
        return "captain_team_" + teamId;
    }
}
