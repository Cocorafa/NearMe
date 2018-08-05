package com.akashapplications.nearme.services;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.akashapplications.nearme.MainActivity;

import java.util.List;


public class LocationService extends Service implements LocationListener {

    protected LocationManager locationManager;
    Location location;

    public LocationService() {
    }

    private static final long MIN_DISTANCE_FOR_UPDATE = 10;
    private static final long MIN_TIME_FOR_UPDATE = 1000;
    Context activityContext;
    public LocationService(Context context) {
        activityContext = context;
        locationManager = (LocationManager) context
                .getSystemService(LOCATION_SERVICE);
    }

    public Location getLocation(String provider) {
//        if (locationManager.isProviderEnabled(provider)) {
            if (ActivityCompat.checkSelfPermission(activityContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("akx","No permission");
                Toast.makeText(activityContext,"Location service terminated as you have denied to give GPS permission",Toast.LENGTH_LONG).show();
                return null;

            }

        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String _provider : providers) {
            Location l = locationManager.getLastKnownLocation(_provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
