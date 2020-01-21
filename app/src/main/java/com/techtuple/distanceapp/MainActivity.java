package com.techtuple.distanceapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;
import com.karan.churi.PermissionManager.PermissionManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    Location location;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    List<Address> addresses;
    private long UPDATE_INTERVAL = 10*1000;
    private long FASTEST_INTERVAL = 2000;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            // Permission is not granted
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        // Create the location request to start receiving updates
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(final int requestCode, String permissions[], int[] grantResults) {
//        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                       Double longitude = location.getLongitude();
                                       Double latitude = location.getLatitude();
                                       Double alt1 = 258.5;
                                        Double alt2 = 0.0;

                                        Double lat = 30.7690;
                                        Double lon = 76.5758;
                                        Double distance = distance(latitude, lat, longitude, lon, alt1, alt2);

                                        Toast.makeText(MainActivity.this, ""+distance/1000, Toast.LENGTH_SHORT).show();
                                       Geocoder geocoder =  new Geocoder(MainActivity.this, Locale.getDefault());
                                        try {
                                            addresses = geocoder.getFromLocation(latitude, longitude, 5);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        LatLng from = new LatLng(latitude, longitude);
                                        LatLng to = new LatLng(31.8818, 76.2146);

                                       // Double distance = SphericalUtil.computeDistanceBetween(from, to);
                                       // Toast.makeText(MainActivity.this, "Distance is: "+distance/1000, Toast.LENGTH_SHORT).show();
                                        Log.d("LOG", "" + addresses);

                                    }
                                    else{
                                        Toast.makeText(MainActivity.this, "Cant fetch location", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else {
                    Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;

            }
            // other 'case' lines to check for other
            // permissions this app might request

        }
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1- el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    protected void createLocationRequest(){
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(30*1000);
        locationRequest.setMaxWaitTime(30*1000);
        locationRequest.setFastestInterval(2*1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    }
}
