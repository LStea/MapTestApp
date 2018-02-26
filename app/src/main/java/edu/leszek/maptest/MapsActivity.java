package edu.leszek.maptest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static edu.leszek.maptest.Location.PLACES_LAT_KEY;
import static edu.leszek.maptest.Location.PLACES_LONG_KEY;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnRequestPermissionsResultCallback {

    private GoogleMap mMap;

    public static final String PLACES_AMOUNT_KEY = "places_amount";

    public static final String PREFERENCES_NAME = "MapsTestPreferences";

    private double mAddedLat;
    private double mAddedLong;
    private boolean mAddedPlace;

    private edu.leszek.maptest.Location mCurrentLocation;
    private double mCurrentLat;
    private double mCurrentLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        final double lat = intent.getDoubleExtra(PLACES_LAT_KEY, 0.0);
        final double lng = intent.getDoubleExtra(PLACES_LONG_KEY, 0.0);
        if (lat != 0.0f || lng != 0.0f) {
            mAddedLat = lat;
            mAddedLong = lng;
            mAddedPlace = true;
        } else {
            mAddedPlace = false;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            askForPermissions();
        }
        else
        {
            // już mamy wystarczające uprawnienia
            continueWithPermissions();
        }
    }

    private void checkLocation(Location location) {
        mCurrentLat = location.getLatitude();
        mCurrentLong = location.getLongitude();

        // sprawdż, czy weszliśmy w którąś z zapamiętanych lokalizacji
        // weź wszystkie lokalizacje
        boolean inAnyLocation = false;
        for (edu.leszek.maptest.Location savedLocation : getAllLocations())
        {
            // czy bieżąca lokalizacja (GPS) leży wewnątrz jednej z zapisanych wcześniej?
            Location temp = new Location(LocationManager.GPS_PROVIDER);
            temp.setLatitude(savedLocation.getPosition().latitude);
            temp.setLongitude(savedLocation.getPosition().longitude);
            temp.setAltitude(location.getAltitude());
            temp.setAccuracy(location.getAccuracy());

            // czy weśliśmy w jakąś lokalizację? (ale inną niż ta, w której już jesteśmy)
            if (location.distanceTo(temp) <= savedLocation.getRadius())
            {
                inAnyLocation = true;
                if (!savedLocation.equals(mCurrentLocation)) {
                    mCurrentLocation = savedLocation;

                    showNotification(savedLocation);
                }
            }
        }

        if (!inAnyLocation) {
            mCurrentLocation = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void continueWithPermissions()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Intent intent = new Intent(SyncStateContract.Constants.ACTION_PROXIMITY_ALERT);
//        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
//
//        if (locationManager != null) {
//            locationManager.addProximityAlert(mCurrentLat,
//                    mCurrentLong, mCurrentLocation.getRadius(), -1, pendingIntent);
//        }

        Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        mCurrentLat = lastLocation.getLatitude();
        mCurrentLong = lastLocation.getLongitude();

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //checkLocation(location);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });

        Button addButton = (Button) findViewById(R.id.addButton);
        final Activity mainActivity = this;
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mainActivity, AddPlaceActivity.class);
                intent.putExtra(PLACES_LAT_KEY, mCurrentLat);
                intent.putExtra(PLACES_LONG_KEY, mCurrentLong);
                mainActivity.startActivity(intent);
            }
        });
    }

    private void showNotification(edu.leszek.maptest.Location location) {
        Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MapsActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Map");
        builder.setContentTitle("Jesteś w lokalizacji " + location.getName())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText("Opis lokalizacji: " + location.getDescription())
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notificationPendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, builder.build());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    continueWithPermissions();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Aplikacja nie ma przyznanych uprawnień do dokładnej lokalizacji", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void askForPermissions()
    {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, 200);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                checkLocation(location);
            }
        });

        for (edu.leszek.maptest.Location location : getAllLocations()) {
            mMap.addCircle(new CircleOptions()
                    .center(location.getPosition())
                    .fillColor(Color.argb(50, 255, 0, 0))
                    .radius(location.getRadius())
                    .strokeWidth(0f));

            mMap.addMarker(new MarkerOptions().position(location.getPosition()).title(location.getName()));
        }

        if (mAddedPlace)
        {
            LatLng justAdded = new LatLng(mAddedLat, mAddedLong);

            mMap.moveCamera(CameraUpdateFactory.zoomTo(15f))    ;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(justAdded));
        }
    }

    private Collection<edu.leszek.maptest.Location> getAllLocations()
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        int savedPlaces = preferences.getInt(PLACES_AMOUNT_KEY, 0);

        if (savedPlaces == 0)
        {
            // dodajmy jakieś przykładowe miejsce
            edu.leszek.maptest.Location location = new edu.leszek.maptest.Location(new LatLng(52.231715, 20.955286), 10f, "Prymasa 95", "Warszawa");
            location.saveToSharedPreferences(0, preferences);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PLACES_AMOUNT_KEY, 1);
            editor.commit();
            savedPlaces = 1;
        }

        Collection<edu.leszek.maptest.Location> result = new ArrayList<>();

        for (int i=0; i<savedPlaces; i++) {
            result.add(edu.leszek.maptest.Location.readFromSharedPreferences (i, preferences));
        }

        return result;
    }
}
