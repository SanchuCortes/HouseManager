package com.example.housemanager.util;

import android.app.Activity;
import android.widget.Toast;

/**
 * Utilidad para lectura segura de leagueId desde Intents/Fragments.
 */
public final class LeagueArgs {
    public static final String EXTRA_LEAGUE_ID = "EXTRA_LEAGUE_ID";
    private LeagueArgs() {}

    /** Lee el leagueId del Intent de la Activity. Finaliza la Activity si falta o es inválido. */
    public static long requireLeagueId(Activity a) {
        long id = -1L;
        try {
            id = a.getIntent().getLongExtra(EXTRA_LEAGUE_ID, -1L);
        } catch (Exception ignored) { }
        if (id <= 0) {
            Toast.makeText(a, "Error: falta leagueId", Toast.LENGTH_SHORT).show();
            a.finish();
        }
        return id;
    }
}
