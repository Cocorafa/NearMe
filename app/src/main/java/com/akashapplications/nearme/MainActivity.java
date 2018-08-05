package com.akashapplications.nearme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akashapplications.nearme.fragments.ExploreFrag;
import com.akashapplications.nearme.fragments.SearchFrag;
import com.akashapplications.nearme.services.LocationAddress;
import com.akashapplications.nearme.services.LocationService;
import com.akashapplications.nearme.utilities.CacheLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.tedpark.tedpermission.rx2.TedRx2Permission;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{


    private SmartTabLayout viewPagerTab;
    LocationService appLocationService;
    static TextView address;
    private GoogleApiClient mClient;
    private int PLACE_PICKER_REQUEST = 12;
    CacheLocation cacheLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkGPSPermission();
//        boolean permissionGranted = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
//
//        if(!permissionGranted)
//            checkGPSPermission();

        cacheLocation = new CacheLocation(getBaseContext());


        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Explore", ExploreFrag.class)
                .add("Search", SearchFrag.class)
                .create());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);


        viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(viewPager);

        address = findViewById(R.id.mainactivityAddress);

        appLocationService = new LocationService(getBaseContext());

        getCurrentLocationLatLng();


        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();


        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    checkGPSPermission();
                }
                try {

                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    Intent i = builder.build(MainActivity.this);
                    startActivityForResult(i, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e("akx", String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e("akx", String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
                } catch (Exception e) {
                    Log.e("akx", String.format("PlacePicker Exception: %s", e.getMessage()));
                }
            }
        });
    }

    public void getCurrentLocationLatLng()
    {
        Location gpsLocation = appLocationService
                .getLocation(LocationManager.GPS_PROVIDER);
        Log.i("akx",""+gpsLocation);
        if (gpsLocation != null) {
            double latitude = gpsLocation.getLatitude();
            double longitude = gpsLocation.getLongitude();
            cacheLocation.saveLocation(latitude,longitude);
            LocationAddress locationAddress = new LocationAddress();
            locationAddress.getAddressFromLocation(latitude, longitude,
                    getApplicationContext(), new GeocoderHandler());
        } else {
            showSettingsAlert();
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);
        alertDialog.setTitle("SETTINGS");
        alertDialog.setMessage("Enable Location Provider! Go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        MainActivity.this.startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkGPSPermission()
    {
        TedRx2Permission.with(getApplicationContext())
                .setRationaleTitle("Allow app to use GPS")
                .setRationaleMessage("We need your permission to access current location") // "we need permission for read contact and find your location"
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION)
                .request()
                .subscribe(tedPermissionResult -> {
                    if (tedPermissionResult.isGranted()) {
                        getCurrentLocationLatLng();
                    } else {
                        Toast.makeText(this,
                                "Permission Denied\n" + tedPermissionResult.getDeniedPermissions().toString(), Toast.LENGTH_SHORT)
                                .show();
                    }
                }, throwable -> {});
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            Log.i("akx",""+locationAddress);
            address.setText(locationAddress);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("akx",requestCode+" "+resultCode+" "+data);
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                Log.i("akx", "No place selected");
                return;
            }
            else{
                Log.i("akx",place.getAddress().toString());
                cacheLocation.saveLocation(place.getLatLng().latitude, place.getLatLng().longitude);
                LocationAddress locationAddress = new LocationAddress();
                locationAddress.getAddressFromLocation(place.getLatLng().latitude, place.getLatLng().longitude,
                        getApplicationContext(), new GeocoderHandler());
            }

        }
    }
}
