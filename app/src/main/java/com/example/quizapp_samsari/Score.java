package com.example.quizapp_samsari;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Score extends AppCompatActivity implements OnMapReadyCallback {

    Button bLogout, bTry;
    ProgressBar progressBar;
    TextView tvScore;
    int score;
    private GoogleMap map;
    private FusedLocationProviderClient locationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        tvScore = findViewById(R.id.tvScore);
        progressBar = findViewById(R.id.progressIndicator);
        bLogout = findViewById(R.id.bLogout);
        bTry = findViewById(R.id.bTry);

        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0);
        progressBar.setProgress(100 * score / 5);
        tvScore.setText(100 * score / 5 + " %");

        bLogout.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Merci de votre Participation !", Toast.LENGTH_SHORT).show();
            finish();
        });

        bTry.setOnClickListener(v -> startActivity(new Intent(Score.this, QuizActivity.class)));

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); // Ensure you have a map fragment in layout
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize location client
        locationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        map.setMyLocationEnabled(true);

        locationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
                map.addMarker(new MarkerOptions().position(pos).title("مكانك الحالي"));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (map != null) {
                onMapReady(map);
            }
        }
    }
}
