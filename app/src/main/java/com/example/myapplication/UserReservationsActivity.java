package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class UserReservationsActivity extends AppCompatActivity {

    private ListView listViewReservations;
    private List<String> reservationsList;
    private ArrayAdapter<String> adapter;
    private Button cancelReservationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_user_reservations);

        listViewReservations = findViewById(R.id.listViewReservations);
        cancelReservationButton = findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        if (intent != null) {
            String hallId = intent.getStringExtra("hallId");
            String startTime = intent.getStringExtra("startTime");
            String endTime = intent.getStringExtra("endTime");
            String numberOfPeople = intent.getStringExtra("numberOfPeople");
            String name = intent.getStringExtra("name");
            String email = intent.getStringExtra("email");
            String phone = intent.getStringExtra("phone");

            // Adăugare detaliile rezervării într-o listă pentru afișare
            List<String> reservationDetails = new ArrayList<>();
            reservationDetails.add("Hall ID: " + hallId);
            reservationDetails.add("Start Time: " + startTime);
            reservationDetails.add("End Time: " + endTime);
            reservationDetails.add("Number of People: " + numberOfPeople);
            reservationDetails.add("Name: " + name);
            reservationDetails.add("Email: " + email);
            reservationDetails.add("Phone: " + phone);

            // Afișare lista într-un ListView
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reservationDetails);
            listViewReservations.setAdapter(adapter);
        }

        // Setare ascultător pentru butonul de anulare a rezervării
        cancelReservationButton.setOnClickListener(v -> {
            //Intent intentNew = getIntent();
            if (intent != null) {
                String hallId = intent.getStringExtra("hallId");
                String startTime = intent.getStringExtra("startTime");
                String endTime = intent.getStringExtra("endTime");
                String numberOfPeople = intent.getStringExtra("numberOfPeople");
                String name = intent.getStringExtra("name");
                String email = intent.getStringExtra("email");
                String phone = intent.getStringExtra("phone");

                // Trimitere date către CancelReservationActivity
                Intent cancelIntent = new Intent(UserReservationsActivity.this, CancelReservationActivity.class);
                cancelIntent.putExtra("hallId", hallId);
                cancelIntent.putExtra("startTime", startTime);
                cancelIntent.putExtra("endTime", endTime);
                cancelIntent.putExtra("numberOfPeople", numberOfPeople);
                cancelIntent.putExtra("name", name);
                cancelIntent.putExtra("email", email);
                cancelIntent.putExtra("phone", phone);
                startActivity(cancelIntent);
            } else {
                // Tratează cazul în care intentul este null
                Toast.makeText(UserReservationsActivity.this, "Intentul este null", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
