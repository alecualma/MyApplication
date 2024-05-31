package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    // Declare any other necessary variables.
    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;


    @Override
    protected void onCreate(Bundle savedInstanceState) { // Metodă apelată la crearea activității
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // Setarea layout-ului pentru activitate

        // Inițializarea instanței FirebaseAuth în metoda onCreate()
        auth = FirebaseAuth.getInstance(); // Obținerea instanței FirebaseAuth
        signupEmail = findViewById(R.id.signup_email); // Găsirea widget-ului EditText pentru email
        signupPassword = findViewById(R.id.signup_password); // Găsirea widget-ului EditText pentru parolă
        signupButton = findViewById(R.id.signup_button); // Găsirea butonului de înscriere
        loginRedirectText = findViewById(R.id.loginRedirectText); // Găsirea textului de redirecționare la login

        signupButton.setOnClickListener(new View.OnClickListener() { // Setarea listener-ului pentru clic pe butonul de înscriere
            @Override
            public void onClick(View view) {
                String user = signupEmail.getText().toString().trim(); // Obține email-ul introdus și elimină spațiile albe
                String pass = signupPassword.getText().toString().trim(); // Obține parola introdusă și elimină spațiile albe

                if (user.isEmpty()){ // Verifică dacă email-ul este gol
                    signupEmail.setError("Email cannot be empty"); // Setează eroarea pentru câmpul de email
                }
                if(pass.isEmpty()){ // Verifică dacă parola este goală
                    signupPassword.setError("Password cannot be empty"); // Setează eroarea pentru câmpul de parolă
                } else{
                    auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() { // Creează un utilizator cu email și parolă
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) { // Metodă apelată la finalizarea sarcinii
                            if(task.isSuccessful()){ // Dacă sarcina este reușită
                                Toast.makeText(SignUpActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show(); // Afișează un mesaj toast de succes
                                startActivity(new Intent(SignUpActivity.this, LoginActivity.class)); // Pornește activitatea de login
                            } else{
                                Toast.makeText(SignUpActivity.this, "Signup Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); // Afișează un mesaj toast de eșec
                            }
                        }
                    });
                }
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() { // Setarea listener-ului pentru clic pe textul de redirecționare la login
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class)); // Pornește activitatea de login
            }
        });

    }
}