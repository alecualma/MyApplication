package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button buttonLogin;
    private Button buttonRegister;
    private Button buttonMap;
    private Button buttonReservation;

    private TextView welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeMessage = findViewById(R.id.welcome_message);
        buttonMap = findViewById(R.id.buttonMap);
        buttonReservation=findViewById(R.id.buttonReservations);
        String username = getIntent().getStringExtra("username");

        // Display the username in the TextView
        welcomeMessage.setText("Welcome, " + username + "!");
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        buttonReservation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UserReservationsActivity.class);
                startActivity(intent);
            }
        });
    }
}
