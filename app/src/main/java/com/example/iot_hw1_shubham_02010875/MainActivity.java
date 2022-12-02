package com.example.iot_hw1_shubham_02010875;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Criteria;
    import android.location.Location;
    import android.location.LocationListener;
    import android.location.LocationManager;
import android.provider.Settings;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Button displayMap;
    //For to find Location
    private TextView locationLatLon;
    private Button locationDisplay;
    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    String latitude, longitude;
    Context mContext;
    //All values
    private double latitudeStart;
    private double longitudeStart;
    private double speed=0;
    private double total_distance=0;
    private double average_speed=0;
    private boolean firsttime=true;
    private int total_time=0;
    private double tdistance=0;
    //Print speed ,average speed and distance
    private TextView speedd;
    private TextView averagespeedd;
    private TextView totaldistanced;
    //For to converte Miles to Meter
    public static final double METERS_IN_MILE = 1609.344;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext=this;
        //Open Map
        displayMap = findViewById(R.id.displayMap);
        displayMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Send latitude and Longitude to MapsActivity
                Bundle bundle = new Bundle();
                bundle.putString("Latitude", String.valueOf(latitudeStart));
                bundle.putString("Longitude", String.valueOf(longitudeStart));
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        //Location Find Code
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
//        locationLatLon = findViewById(R.id.locationLatLon);
        locationDisplay = findViewById(R.id.locationDisplay);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationManager nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("Location Permission", "False");
            OnGPS();
        } else {
            Log.d("Location Permission", "True");
            getLocation();
        }
        //Manual Testing
        locationDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.d("Location Permission", "False");
                    OnGPS();
                } else {
                    Log.d("Location Permission", "True");
                    getLocation();
                }
            }
        });
    }

    //Location Listener
    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if(firsttime==true){
                latitudeStart=location.getLatitude();
                longitudeStart = location.getLongitude();
                firsttime=false;
                displayMap.setEnabled(true);
                displayMap.setText("View Map");
            }else{
                tdistance=distance(latitudeStart,location.getLatitude(),longitudeStart,location.getLongitude());
                total_distance=total_distance+tdistance;
                speed=tdistance/10;
                latitudeStart=location.getLatitude();
                longitudeStart=location.getLongitude();
                total_time=total_time+10;
                average_speed=total_distance/total_time;
                Log.d("total distance",String.valueOf(total_distance));
                Log.d("speed",String.valueOf(speed));
                Log.d("total_time",String.valueOf(total_time));
                Log.d("average_speed",String.valueOf(average_speed));
                Log.d("distance",String.valueOf(tdistance));
                //Display speed,total distance and average speed
                speedd = findViewById(R.id.speedDisplay);
                speedd.setText(String.valueOf(speed));

                averagespeedd = findViewById(R.id.aspeedDisplay);
                averagespeedd.setText(String.valueOf(average_speed));

                totaldistanced=findViewById(R.id.distanceDisplay);
                totaldistanced.setText(String.valueOf(total_distance));
                tdistance=0;
                speed=0;
            }
            String msg = "New Latitude: " + location.getLatitude() + "New Longitude: " + location.getLongitude();
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, locationListenerGPS);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
//            List<String> providers = locationManager.getProviders(true);
//            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            Location location=getLastKnownLocation();
//            if (location != null) {
//                double lat = location.getLatitude();
//                double longi = location.getLongitude();
//                latitude = String.valueOf(lat);
//                longitude = String.valueOf(longi);
//                locationDisplay.setText("Your Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude);
//            } else {
//                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
//            }
            //Set update criteria to 10000milisecond=10second and 0.3 meter
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, (float)0.1, locationListenerGPS);
        }
    }
    //Distance Calculation
    public static double distance(double lat1,
                                  double lat2, double lon1,
                                  double lon2)
    {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(c * r)*METERS_IN_MILE;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}