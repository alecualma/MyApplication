package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private PolylineOptions lineOptions;
    private double totalDistance = 0;
    private LocationManager locationManager;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private Map<Marker, DataSnapshot> markerSnapshotMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        lineOptions = new PolylineOptions().width(20).color(Color.RED);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("sport_halls");

        fetchSportHalls();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // Activează butoanele de zoom și de navigare
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Setează locația curentă implicită în Iași, strada Costache Negri nr. 11
        LatLng iasiLocation = new LatLng(47.1741, 27.5749);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iasiLocation, 13));

        // Marchează locația din Iași cu un punct albastru
        mMap.addMarker(new MarkerOptions()
                .position(iasiLocation)
                .title("Iași")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Setează raza
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iasiLocation, 13));

        // Afișează marcatorii pentru sălile de sport
        fetchSportHalls();
    }

    private void fetchSportHalls() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    double latitude = snapshot.child("latitude").getValue(Double.class);
                    double longitude = snapshot.child("longitude").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);

                    // Creează un obiect LatLng pentru locația sălii de sport curente
                    LatLng location = new LatLng(latitude, longitude);

                    // Adaugă un marcator de tip pin roșu pentru locația sălii de sport
                    Marker marker = mMap.addMarker(new MarkerOptions().position(location).title(name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                    // Activează afișarea titlului
                    marker.showInfoWindow();

                    // Mapăm marcatorul la snapshot-ul Firebase corespunzător
                    markerSnapshotMap.put(marker, snapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Error fetching data", databaseError.toException());
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        DataSnapshot snapshot = markerSnapshotMap.get(marker);
        if (snapshot != null) {
            showSportHallDetailsDialog(snapshot);
        }
        return true;
    }

    private void showSportHallDetailsDialog(DataSnapshot snapshot) {
        String name = snapshot.child("name").getValue(String.class);
        String address = snapshot.child("address").getValue(String.class);
        String hours = snapshot.child("hours").getValue(String.class);
        DataSnapshot availabilitySnapshot = snapshot.child("availability");
        DataSnapshot pricesSnapshot = snapshot.child("prices");
        String hallId = snapshot.getKey(); // Get the hallId

        // Obținem ora curentă
        String currentTime = android.text.format.DateFormat.format("HH:mm", new java.util.Date()).toString();

        // Verificăm disponibilitatea pentru intervalul orar în care ne aflăm
        String currentAvailability = "Indisponibil";
        String currentPrice = "Nedeterminat";
        for (DataSnapshot slotSnapshot : availabilitySnapshot.getChildren()) {
            String slot = slotSnapshot.getKey();
            if (isSlotAvailable(slot, currentTime)) {
                currentAvailability = "Disponibil";
                // Obținem prețul pentru intervalul orar curent
                currentPrice = getPriceForSlot(slot, pricesSnapshot);
                break; // Dacă găsim un interval disponibil, nu mai căutăm în continuare
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sport_hall_details, null);
        builder.setView(dialogView);

        TextView nameTextView = dialogView.findViewById(R.id.nameTextView);
        TextView addressTextView = dialogView.findViewById(R.id.addressTextView);
        TextView hoursTextView = dialogView.findViewById(R.id.hoursTextView);
        TextView availabilityTextView = dialogView.findViewById(R.id.availabilityTextView);
        TextView priceTextView = dialogView.findViewById(R.id.priceTextView); // Utilizăm id-ul corect pentru preț
        Button reserveButton = dialogView.findViewById(R.id.reserveButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);

        nameTextView.setText(name);
        addressTextView.setText(address);
        hoursTextView.setText(hours);
        availabilityTextView.setText(currentAvailability);
        priceTextView.setText(currentPrice);

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass hallId to ReservationActivity
                Intent intent = new Intent(MapsActivity.this, ReservationActivity.class);
                intent.putExtra("hallId", hallId);
                startActivity(intent);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass hallId to ReservationActivity
                Intent intent = new Intent(MapsActivity.this, CancelReservationActivity.class);
                intent.putExtra("hallId", hallId);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Închide", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Metoda pentru a obține prețul pentru un anumit interval orar
    private String getPriceForSlot(String slot, DataSnapshot pricesSnapshot) {
        for (DataSnapshot priceSnapshot : pricesSnapshot.getChildren()) {
            String priceSlot = priceSnapshot.getKey();
            if (isSlotInRange(slot, priceSlot)) {
                return priceSnapshot.getValue(String.class);
            }
        }
        return "Nedeterminat";
    }

    // Metodă pentru a verifica dacă intervalul orar specificat se află în intervalul prețului
    private boolean isSlotInRange(String slot, String priceSlot) {
        String[] slotParts = slot.split(" - ");
        String[] priceSlotParts = priceSlot.split(" - ");

        // Convertim intervalele orare în ore și minute pentru comparație
        int slotStartHour = Integer.parseInt(slotParts[0].split(":")[0]);
        int slotStartMinute = Integer.parseInt(slotParts[0].split(":")[1]);
        int slotEndHour = Integer.parseInt(slotParts[1].split(":")[0]);
        int slotEndMinute = Integer.parseInt(slotParts[1].split(":")[1]);

        int priceSlotStartHour = Integer.parseInt(priceSlotParts[0].split(":")[0]);
        int priceSlotStartMinute = Integer.parseInt(priceSlotParts[0].split(":")[1]);
        int priceSlotEndHour = Integer.parseInt(priceSlotParts[1].split(":")[0]);
        int priceSlotEndMinute = Integer.parseInt(priceSlotParts[1].split(":")[1]);

        // Verificăm dacă intervalul orar specificat se încadrează în intervalul prețului
        if ((slotStartHour > priceSlotStartHour || (slotStartHour == priceSlotStartHour && slotStartMinute >= priceSlotStartMinute)) &&
                (slotEndHour < priceSlotEndHour || (slotEndHour == priceSlotEndHour && slotEndMinute <= priceSlotEndMinute))) {
            return true;
        }
        return false;
    }

    // Verifică dacă un slot este disponibil în funcție de intervalul orar și ora curentă
    private boolean isSlotAvailable(String slot, String currentTime) {
        String[] slotParts = slot.split(" - ");
        String[] currentParts = currentTime.split(":");
        int currentHour = Integer.parseInt(currentParts[0]);
        int currentMinute = Integer.parseInt(currentParts[1]);

        String[] startParts = slotParts[0].split(":");
        int startHour = Integer.parseInt(startParts[0]);
        int startMinute = Integer.parseInt(startParts[1]);

        String[] endParts = slotParts[1].split(":");
        int endHour = Integer.parseInt(endParts[0]);
        int endMinute = Integer.parseInt(endParts[1]);

        // Verificăm dacă ora curentă se află între intervalul de disponibilitate
        if (currentHour > startHour || (currentHour == startHour && currentMinute >= startMinute)) {
            if (currentHour < endHour || (currentHour == endHour && currentMinute <= endMinute)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("Location", "Current location coordinates: " + location.getLatitude() + ", " + location.getLongitude());
        computeAndDisplayPoints(newLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Neimplementat
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Neimplementat
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Neimplementat
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {

    }

    private void computeAndDisplayPoints(LatLng newPoint) {
        Location lastLocation = getLastLocation();
        if (lastLocation != null) {
            Location newLocation = new Location("");
            newLocation.setLatitude(newPoint.latitude);
            newLocation.setLongitude(newPoint.longitude);

            // Logăm coordonatele locației curente
            Log.d("Location", "New location coordinates: " + newLocation.getLatitude() + ", " + newLocation.getLongitude());

            // Obținem adresa locației curente utilizând Geocoderul
            Geocoder geocoder = new Geocoder(this, Locale.ROOT);

            try {
                List<Address> addresses = geocoder.getFromLocation(newLocation.getLatitude(), newLocation.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String fullAddress = address.getAddressLine(0);
                    Log.i("Location", "Current address: " + fullAddress);
                    Toast.makeText(this, "Current address: " + fullAddress, Toast.LENGTH_LONG).show();
                } else {
                    Log.e("Location", "No address found");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Location", "Error getting address: " + e.getMessage());
            }

            // Adăugăm distanța la totalDistance și afișăm un mesaj
            double distance = 0;
            totalDistance += distance;
            Toast.makeText(this, "Total distance = " + totalDistance / 1000 + " kilometers", Toast.LENGTH_LONG).show();

            // Adăugăm punctul la linia poligonală și actualizăm harta
            lineOptions.add(newPoint);
            if (mMap != null) {
                mMap.addPolyline(lineOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newPoint));
            }
        }
    }

    private Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return null;
    }
}
