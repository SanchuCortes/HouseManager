package com.example.housemanager.database;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.housemanager.database.entities.Team;
import com.example.housemanager.database.entities.Player;
import com.example.housemanager.database.dao.TeamDao;
import com.example.housemanager.database.dao.PlayerDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {Team.class, Player.class},
        version = 1,
        exportSchema = false
)
public abstract class HouseManagerDatabase extends RoomDatabase {

    private static volatile HouseManagerDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // ExecutorService para operaciones de base de datos en background
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // DAOs abstractos
    public abstract TeamDao teamDao();
    public abstract PlayerDao playerDao();

    /**
     * Obtiene la instancia singleton de la base de datos
     */
    public static HouseManagerDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (HouseManagerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    HouseManagerDatabase.class,
                                    "housemanager_database"
                            )
                            // Permitir consultas en el hilo principal solo para desarrollo
                            // En producción, usar siempre background threads
                            .allowMainThreadQueries()
                            // Añadir callback para poblar datos iniciales
                            .addCallback(roomDatabaseCallback)
                            // Añadir migraciones futuras aquí
                            //.addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback que se ejecuta cuando se crea la base de datos por primera vez
     */
    private static RoomDatabase.Callback roomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Poblar la base de datos en background
            databaseWriteExecutor.execute(() -> {
                // Llamar a DAOs para insertar datos iniciales si es necesario
                TeamDao teamDao = INSTANCE.teamDao();
                PlayerDao playerDao = INSTANCE.playerDao();

                // Los datos reales vendrán de la API, pero podemos insertar algunos datos mock iniciales
                populateInitialData(teamDao, playerDao);
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Base de datos abierta, realizar cualquier configuración necesaria
        }
    };

    /**
     * Poblar datos iniciales (solo si no hay datos de la API)
     */
    private static void populateInitialData(TeamDao teamDao, PlayerDao playerDao) {
        // Solo insertar datos mock si no hay equipos en la BD
        if (teamDao.getTeamsCount() == 0) {
            // Los datos reales vendrán de la API de football-data.org
            // Este es solo un placeholder por si la API falla
            insertMockTeamIfNeeded(teamDao, playerDao);
        }
    }

    /**
     * Insertar un equipo mock solo como backup
     */
    private static void insertMockTeamIfNeeded(TeamDao teamDao, PlayerDao playerDao) {
        // Equipo mock - Real Madrid
        Team realMadrid = new Team(
                86, // ID real del Real Madrid en la API
                "Real Madrid CF",
                "Real Madrid",
                "RMA",
                "https://crests.football-data.org/86.png"
        );
        realMadrid.setPosition(1);
        realMadrid.setPoints(50);
        realMadrid.setPlayedGames(20);
        realMadrid.setWon(16);
        realMadrid.setDraw(2);
        realMadrid.setLost(2);
        realMadrid.setGoalsFor(45);
        realMadrid.setGoalsAgainst(15);
        realMadrid.setGoalDifference(30);

        teamDao.insertTeam(realMadrid);

        // Algunos jugadores mock del Real Madrid
        Player[] mockPlayers = {
                new Player(44, "Thibaut Courtois", "GK", 86, "Real Madrid CF"),
                new Player(5098, "Dani Carvajal", "DEF", 86, "Real Madrid CF"),
                new Player(3046, "Luka Modrić", "MID", 86, "Real Madrid CF"),
                new Player(3499, "Karim Benzema", "FWD", 86, "Real Madrid CF")
        };

        for (Player player : mockPlayers) {
            player.setNationality("Spain"); // Simplificado
            player.setShirtNumber(player.getPlayerId() % 30 + 1); // Número mock
            playerDao.insertPlayer(player);
        }
    }

    /**
     * Cierra la base de datos cuando la app se cierra
     */
    public static void closeDatabase() {
        if (INSTANCE != null) {
            INSTANCE.close();
            INSTANCE = null;
        }
        databaseWriteExecutor.shutdown();
    }

    /**
     * Limpia todos los datos de la base de datos
     */
    public void clearAllData() {
        databaseWriteExecutor.execute(() -> {
            playerDao().deleteAllPlayers();
            teamDao().deleteAllTeams();
        });
    }

    /**
     * Obtiene estadísticas de la base de datos
     */
    public void getDatabaseStats(DatabaseStatsCallback callback) {
        databaseWriteExecutor.execute(() -> {
            int teamsCount = teamDao().getTeamsCount();
            int playersCount = playerDao().getPlayersCount();
            int availablePlayersCount = playerDao().getAvailablePlayersCount().getValue();
            long lastSync = Math.max(
                    teamDao().getLastSyncTime(),
                    playerDao().getLastSyncTime()
            );

            DatabaseStats stats = new DatabaseStats(teamsCount, playersCount, availablePlayersCount, lastSync);
            callback.onStatsReady(stats);
        });
    }

    // ================ MIGRACIONES FUTURAS ================

    /**
     * Migración de versión 1 a 2 (ejemplo para futuras versiones)
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Ejemplo: Añadir nueva columna
            // database.execSQL("ALTER TABLE teams ADD COLUMN new_column TEXT");
        }
    };

    /**
     * Migración de versión 2 a 3 (ejemplo)
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Ejemplo: Crear nueva tabla
            // database.execSQL("CREATE TABLE new_table (id INTEGER PRIMARY KEY NOT NULL, name TEXT)");
        }
    };

    // ================ INTERFACES Y CLASES AUXILIARES ================

    /**
     * Callback para estadísticas de la base de datos
     */
    public interface DatabaseStatsCallback {
        void onStatsReady(DatabaseStats stats);
    }

    /**
     * Clase para estadísticas de la base de datos
     */
    public static class DatabaseStats {
        public final int teamsCount;
        public final int playersCount;
        public final int availablePlayersCount;
        public final long lastSyncTime;

        public DatabaseStats(int teamsCount, int playersCount, int availablePlayersCount, long lastSyncTime) {
            this.teamsCount = teamsCount;
            this.playersCount = playersCount;
            this.availablePlayersCount = availablePlayersCount;
            this.lastSyncTime = lastSyncTime;
        }

        @Override
        public String toString() {
            return "DatabaseStats{" +
                    "teams=" + teamsCount +
                    ", players=" + playersCount +
                    ", available=" + availablePlayersCount +
                    ", lastSync=" + lastSyncTime +
                    '}';
        }
    }

    /**
     * Verifica la integridad de la base de datos
     */
    public void checkDatabaseIntegrity(IntegrityCallback callback) {
        databaseWriteExecutor.execute(() -> {
            try {
                // Verificar que todos los jugadores tienen equipos válidos
                boolean integrityOk = true;
                StringBuilder errors = new StringBuilder();

                // Verificar foreign keys
                List<Player> playersWithoutTeam = playerDao().getAllPlayersSync();
                for (Player player : playersWithoutTeam) {
                    Team team = teamDao().getTeamByIdSync(player.getTeamId());
                    if (team == null) {
                        integrityOk = false;
                        errors.append("Player ").append(player.getName())
                                .append(" has invalid team_id: ").append(player.getTeamId()).append("\n");
                    }
                }

                // Verificar duplicados
                List<Integer> playerIds = playerDao().getPlayerIdsByPosition("GK");
                if (playerIds.size() != playerIds.stream().distinct().count()) {
                    integrityOk = false;
                    errors.append("Duplicate player IDs found\n");
                }

                callback.onIntegrityCheckComplete(integrityOk, errors.toString());

            } catch (Exception e) {
                callback.onIntegrityCheckComplete(false, "Error checking integrity: " + e.getMessage());
            }
        });
    }

    /**
     * Callback para verificación de integridad
     */
    public interface IntegrityCallback {
        void onIntegrityCheckComplete(boolean isValid, String errors);
    }
}