package com.example.housemanager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Elementos del login
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnGoogleSignin;

    // TextViews para enlaces
    private TextView tvRegister;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Iniciando LoginActivity");
        setContentView(R.layout.activity_login);

        initViews();
        setupButtonListeners();

        Log.d(TAG, "onCreate: LoginActivity iniciada correctamente");
    }

    // Conectar todas las vistas con sus IDs
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleSignin = findViewById(R.id.btn_google_signin);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
    }

    // Configurar que pasa cuando clickeas los botones
    private void setupButtonListeners() {

        // Botón Iniciar Sesión - va directo al MainActivity
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botón Iniciar Sesión clickeado");

                // Por ahora no validamos nada, solo navegamos
                goToMainActivity();
            }
        });

        // Enlaces clickeables
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Registro clickeado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                // Aquí iría la navegación a registro
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Recuperar contraseña clickeado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                // Aquí iría la navegación a recuperar contraseña
            }
        });

        // Botón Google Sign-In - también va al MainActivity
        btnGoogleSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botón Google Sign-In clickeado");

                // Aquí iría la lógica de Google, pero por ahora navegamos directo
                com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content), "Google Sign-In clickeado", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                goToMainActivity();
            }
        });
    }

    // Navegar al MainActivity
    private void goToMainActivity() {
        Log.d(TAG, "Navegando al MainActivity");

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

        // Cerrar LoginActivity para que no pueda volver con botón atrás
        finish();
    }

    // Para mostrar mensajes rápidos (debug)
    private void showToast(String message) {
        // Reemplazado por Snackbar en llamadas de UI
        Log.d(TAG, message);
    }
}