package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservationActivity extends AppCompatActivity {
    private Spinner daySpinner;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private Spinner startTimeSpinner;
    private Spinner endTimeSpinner;
    private Spinner trainingTypeSpinner;
    private EditText numberOfPeopleEditText;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button reserveButton;
    private String hallIdCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // Extrage hallId din intent
        hallIdCurrent = getIntent().getStringExtra("hallId");

        daySpinner = findViewById(R.id.daySpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        startTimeSpinner = findViewById(R.id.startTimeSpinner);
        endTimeSpinner = findViewById(R.id.endTimeSpinner);
        numberOfPeopleEditText = findViewById(R.id.numberOfPeopleEditText);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        reserveButton = findViewById(R.id.reserveButton);

        // Populează spinners cu ore și adăugă ascultători pentru selecție
        populateTimeSpinners(); // Adăugarea acestei linii pentru a apela metoda de populare a spinner-elor de timp

        // Setare ascultător pentru butonul de rezervare
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Obține data selectată din spinnere
                    /*String day = daySpinner.getSelectedItem() != null ? daySpinner.getSelectedItem().toString() : "";
                    String month = monthSpinner.getSelectedItem() != null ? monthSpinner.getSelectedItem().toString() : "";
                    String year = yearSpinner.getSelectedItem() != null ? yearSpinner.getSelectedItem().toString() : "";
                    String date = day + "." + month + "." + year;*/

                    // Obține alte date introduse în formular
                    String startTime = startTimeSpinner.getSelectedItem() != null ? startTimeSpinner.getSelectedItem().toString() : "";
                    String endTime = endTimeSpinner.getSelectedItem() != null ? endTimeSpinner.getSelectedItem().toString() : "";
                    String numberOfPeople = numberOfPeopleEditText.getText() != null ? numberOfPeopleEditText.getText().toString() : "";
                    String name = nameEditText.getText() != null ? nameEditText.getText().toString() : "";
                    String email = emailEditText.getText() != null ? emailEditText.getText().toString() : "";
                    String phone = phoneEditText.getText() != null ? phoneEditText.getText().toString() : "";

                    // Verifică disponibilitatea și actualizează baza de date Firebase
                    checkAvailabilityAndUpdateFirebase(hallIdCurrent, startTime, endTime);
                } catch (Exception e) {
                    Log.e("ReservationActivity", "Error occurred: " + e.getMessage(), e);
                    Toast.makeText(ReservationActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateTimeSpinners() {
        // Definirea intervalelor de timp disponibile (în minute)
        int openingHour = 700; // 7:00
        int closingHour = 2200; // 22:00

        // Crearea listei de ore disponibile
        List<String> hoursList = new ArrayList<>();
        for (int hour = openingHour; hour <= closingHour; hour += 100) {
            // Adăugarea orei curente în listă în formatul HH:mm
            hoursList.add(String.format(Locale.getDefault(), "%02d:00", hour / 100));
        }

        // Crearea adaptorului pentru spinners
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hoursList);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Setarea adaptorului pentru spinners
        startTimeSpinner.setAdapter(timeAdapter);
        endTimeSpinner.setAdapter(timeAdapter);

        // Popularea spinner-elor pentru zi, lună și an
        populateDateSpinners();
    }

    private void populateDateSpinners() {
        // Crearea listelor pentru zile, luni și ani
        List<String> daysList = new ArrayList<>();
        for (int day = 1; day <= 31; day++) {
            daysList.add(String.format(Locale.getDefault(), "%02d", day));
        }

        List<String> monthsList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            monthsList.add(String.format(Locale.getDefault(), "%02d", month));
        }

        List<String> yearsList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int year = currentYear; year <= currentYear + 5; year++) {
            yearsList.add(String.format(Locale.getDefault(), "%04d", year));
        }

        // Crearea adaptorilor pentru spinners
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, daysList);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthsList);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, yearsList);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Setarea adaptorilor pentru spinners
        daySpinner.setAdapter(dayAdapter);
        monthSpinner.setAdapter(monthAdapter);
        yearSpinner.setAdapter(yearAdapter);
    }

    // Metoda pentru verificarea disponibilității și actualizarea datelor în Firebase
    // Metoda pentru verificarea disponibilității și actualizarea datelor în Firebase
    private void checkAvailabilityAndUpdateFirebase(String hallId, String startTime, String endTime) {
        Log.d("ReservationActivity", "checkAvailabilityAndUpdateFirebase: Entering method");

        // Get a reference to the Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hallAvailabilityRef = database.getReference("sport_halls").child(hallId).child("availability");

        // Check if the selected time slot is available
        hallAvailabilityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String timeSlotKey = startTime + " - " + endTime;
                if (snapshot.hasChild(timeSlotKey)) {
                    String availability = snapshot.child(timeSlotKey).getValue(String.class);
                    if (availability != null && availability.equals("Disponibil")) {
                        // The time slot is available, reserve it
                        Map<String, Object> updateValues = new HashMap<>();
                        updateValues.put(timeSlotKey, "Indisponibil");

                        hallAvailabilityRef.updateChildren(updateValues)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Update was successful
                                        Toast.makeText(ReservationActivity.this, "Reservation successful", Toast.LENGTH_SHORT).show();

                                        // Transfer data to UserReservationsActivity
                                        Intent intent = new Intent(ReservationActivity.this, UserReservationsActivity.class);
                                        intent.putExtra("hallId", hallId);
                                        intent.putExtra("startTime", startTime);
                                        intent.putExtra("endTime", endTime);
                                        intent.putExtra("numberOfPeople", numberOfPeopleEditText.getText().toString());
                                        intent.putExtra("name", nameEditText.getText().toString());
                                        intent.putExtra("email", emailEditText.getText().toString());
                                        intent.putExtra("phone", phoneEditText.getText().toString());
                                        startActivity(intent);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // There was an error during the update
                                        Toast.makeText(ReservationActivity.this, "Failed to reserve the time slot", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // The time slot is unavailable
                        Toast.makeText(ReservationActivity.this, "Selected time slot is unavailable", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // The time slot does not exist, assume it is available and reserve it
                    Map<String, Object> updateValues = new HashMap<>();
                    updateValues.put(timeSlotKey, "Indisponibil");

                    hallAvailabilityRef.updateChildren(updateValues)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Update was successful
                                    Toast.makeText(ReservationActivity.this, "Reservation successful", Toast.LENGTH_SHORT).show();

                                    // Transfer data to UserReservationsActivity
                                    Intent intent = new Intent(ReservationActivity.this, UserReservationsActivity.class);
                                    intent.putExtra("hallId", hallId);
                                    intent.putExtra("startTime", startTime);
                                    intent.putExtra("endTime", endTime);
                                    intent.putExtra("numberOfPeople", numberOfPeopleEditText.getText().toString());
                                    intent.putExtra("name", nameEditText.getText().toString());
                                    intent.putExtra("email", emailEditText.getText().toString());
                                    intent.putExtra("phone", phoneEditText.getText().toString());
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // There was an error during the update
                                    Toast.makeText(ReservationActivity.this, "Failed to reserve the time slot", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // There was an error while reading the data
                Toast.makeText(ReservationActivity.this, "Failed to check availability", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
