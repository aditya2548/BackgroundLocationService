package com.example.backgroundlocationservice;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class BatchLocationActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "MyTag";
    private TextView mOutputText;
    private Button mBtnLocationRequest, mBtnStartService, mBtnStopService;
    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_location);


        mOutputText = findViewById(R.id.tv_output);
        mBtnLocationRequest = findViewById(R.id.btn_location_request);
        mBtnStartService = findViewById(R.id.btn_start_service);
        mBtnStopService = findViewById(R.id.btn_stop_service);

        mLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    Log.d(TAG, "onLocationResult: location error");
                    return;
                }

                List<Location> locations = locationResult.getLocations();

                LocationResultHelper helper = new LocationResultHelper(BatchLocationActivity.this, locations);

                helper.showNotification();

                helper.saveLocationResults();

                Toast.makeText(BatchLocationActivity.this, "Location received: " + locations.size(), Toast.LENGTH_SHORT).show();

                mOutputText.setText(helper.getLocationResultText());

            }
        };

        mBtnLocationRequest.setOnClickListener(this::requestBatchLocationUpdates);
        mBtnStartService.setOnClickListener(this::startLocationService);
        mBtnStopService.setOnClickListener(this::stopLocationService);

    }

    private void startLocationService(View view) {
        //start background location service

        Intent intent = new Intent(this, MyBackgroundLocationService.class);
        ContextCompat.startForegroundService(this, intent);
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

    }

    private void stopLocationService(View view) {
        //stop background location service

        Intent intent = new Intent(this, MyBackgroundLocationService.class);
        stopService(intent);
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();

    }

    private void requestBatchLocationUpdates(View view) {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(4000);

        locationRequest.setMaxWaitTime(15 * 1000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Grant location permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOutputText.setText(LocationResultHelper.getSavedLocationResults(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(LocationResultHelper.KEY_LOCATION_RESULTS)) {
            mOutputText.setText(LocationResultHelper.getSavedLocationResults(this));
        }

    }
}