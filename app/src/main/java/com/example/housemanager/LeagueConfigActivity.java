package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LeagueConfigActivity extends AppCompatActivity {

    // Views principales
    private TextInputLayout tilLeagueName;
    private TextInputEditText etLeagueName;
    private Spinner spinnerBudget;
    private Spinner spinnerMarketHour;
    private ChipGroup chipGroupInitialTeam;
    private Switch switchClauseRobbery;
    private Switch switchLoans;
    private SeekBar sliderBlockDays;
    private TextView tvBlockDays;
    private Switch switchCaptain;
    private Switch switchBench;
    private Switch switchCoach;
    private MaterialButton btnCreateLeague;
    private MaterialButton btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_config);

        // Configurar toolbar
        setupToolbar();

        // Inicializar views
        initializeViews();

        // Configurar spinners
        setupSpinners();

        // Configurar listeners
        setupListeners();
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Configurar Liga Privada");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeViews() {
        tilLeagueName = findViewById(R.id.til_league_name);
        etLeagueName = findViewById(R.id.et_league_name);
        spinnerBudget = findViewById(R.id.spinner_budget);
        spinnerMarketHour = findViewById(R.id.spinner_market_hour);
        chipGroupInitialTeam = findViewById(R.id.chip_group_initial_team);
        switchClauseRobbery = findViewById(R.id.switch_clause_robbery);
        switchLoans = findViewById(R.id.switch_loans);
        sliderBlockDays = findViewById(R.id.slider_block_days);
        tvBlockDays = findViewById(R.id.tv_block_days);
        switchCaptain = findViewById(R.id.switch_captain);
        switchBench = findViewById(R.id.switch_bench);
        switchCoach = findViewById(R.id.switch_coach);
        btnCreateLeague = findViewById(R.id.btn_create_league);
        btnCancel = findViewById(R.id.btn_cancel);
    }

    private void setupSpinners() {
        // Spinner presupuesto inicial
        String[] budgetOptions = {
                "100M €", "110M €", "120M €", "130M €", "140M €",
                "150M €", "160M €", "170M €", "180M €", "190M €", "200M €"
        };
        ArrayAdapter<String> budgetAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, budgetOptions);
        budgetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBudget.setAdapter(budgetAdapter);
        spinnerBudget.setSelection(5); // 150M por defecto

        // Spinner hora actualización mercado - 24 horas
        String[] hourOptions = new String[24];
        for (int i = 0; i < 24; i++) {
            hourOptions[i] = String.format("%02d:00", i);
        }
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hourOptions);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarketHour.setAdapter(hourAdapter);
        spinnerMarketHour.setSelection(14); // 14:00 por defecto
    }

    private void setupListeners() {
        // SeekBar días de bloqueo
        sliderBlockDays.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBlockDays.setText(progress + " días");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Botón crear liga
        btnCreateLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLeague();
            }
        });

        // Botón cancelar
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createLeague() {
        String leagueName = etLeagueName.getText().toString().trim();

        // Validar nombre obligatorio
        if (leagueName.isEmpty()) {
            tilLeagueName.setError("El nombre de la liga es obligatorio");
            return;
        }

        // Validar longitud máxima
        if (leagueName.length() > 30) {
            tilLeagueName.setError("El nombre no puede superar 30 caracteres");
            return;
        }

        // Limpiar error si había
        tilLeagueName.setError(null);

        // Obtener configuración del formulario
        String budget = (String) spinnerBudget.getSelectedItem();
        String marketHour = (String) spinnerMarketHour.getSelectedItem();

        // Obtener tipo de equipo inicial seleccionado
        String teamType = getSelectedTeamType();

        boolean clauseRobbery = switchClauseRobbery.isChecked();
        boolean loans = switchLoans.isChecked();
        int blockDays = sliderBlockDays.getProgress();
        boolean captain = switchCaptain.isChecked();
        boolean bench = switchBench.isChecked();
        boolean coach = switchCoach.isChecked();

        // Guardar la liga usando LeagueManager
        LeagueManager.League newLeague = saveLeague(leagueName, budget, marketHour, teamType);

        if (newLeague != null) {
            // Mostrar mensaje de éxito
            Toast.makeText(this,
                    "¡Liga '" + leagueName + "' creada con éxito!\n" +
                            "Presupuesto: " + budget + "\n" +
                            "Hora mercado: " + marketHour,
                    Toast.LENGTH_LONG).show();

            // Volver a la pantalla de ligas
            Intent intent = new Intent(this, LeaguesActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error al crear la liga. Inténtalo de nuevo.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método auxiliar para obtener el tipo de equipo seleccionado
    private String getSelectedTeamType() {
        int selectedChipId = chipGroupInitialTeam.getCheckedChipId();

        if (selectedChipId == R.id.chip_empty_team) {
            return "Equipo Vacío";
        } else if (selectedChipId == R.id.chip_random_100m) {
            return "Aleatorio 100M";
        } else if (selectedChipId == R.id.chip_random_120m) {
            return "Aleatorio 120M";
        } else if (selectedChipId == R.id.chip_random_150m) {
            return "Aleatorio 150M";
        } else {
            return "Equipo Vacío"; // Por defecto
        }
    }

    // Método para guardar la liga en el manager
    private LeagueManager.League saveLeague(String name, String budget, String marketHour, String teamType) {
        try {
            // Crear y guardar la liga usando LeagueManager
            LeagueManager.League newLeague = LeagueManager.getInstance().createLeague(
                    name,
                    budget,
                    marketHour,
                    teamType,
                    true // siempre es privada desde esta pantalla
            );

            return newLeague;
        } catch (Exception e) {
            // Log del error (en una app real usarías Log.e)
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}