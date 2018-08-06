package com.akashapplications.nearme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import mehdi.sakout.fancybuttons.FancyButton;

public class Splash extends Activity {
    private Context context;
    private Activity activity;
    private static final int PERMISSION_REQUEST_CODE = 1;
    FancyButton btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        context = getApplicationContext();
        activity = this;

        btn = findViewById(R.id.splash_permission);
        btn.setVisibility(View.GONE);


        if(!checkPermission())
            requestPermission();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(3000);
                // Now change the color back. Needs to be done on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                            if(checkPermission())
                            {
                                startActivity(new Intent(getBaseContext(),MainActivity.class));
                                finish();

                            }
                            else{
                                btn.setVisibility(View.VISIBLE);

                                Toast.makeText(context,"Sorry, we cannot proceed without Location permission.", Toast.LENGTH_LONG).show();

                            }

                    }
                });
            }
        }).start();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
            }
        });

    }


    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }

    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,android.Manifest.permission.ACCESS_FINE_LOCATION)){

            Toast.makeText(context,"GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

//        } else {

            ActivityCompat.requestPermissions(activity,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivity(new Intent(getBaseContext(),MainActivity.class));
                    finish();
                } else {
                    btn.setVisibility(View.VISIBLE);

                    Toast.makeText(context,"Soory, we cannot proceed without Location permission.", Toast.LENGTH_LONG).show();


                }
                break;
        }
    }



}

