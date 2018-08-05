package com.akashapplications.nearme;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class PlaceImage extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            placeImage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_place_image);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        getSupportActionBar().setTitle(getIntent().getStringExtra("name"));
        init();

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);


        // Set up the user interaction to manually show or hide the system UI.
        placeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);


    }


    private static final int PLACE_PICKER_REQ_CODE = 2;

    private GeoDataClient geoDataClient;
    private ImageView placeImage;
    private List<PlacePhotoMetadata> photosDataList;
    private int currentPhotoIndex = 0;
    FancyButton prevPhoto,nextPhoto;

    private void init() {

        prevPhoto = findViewById(R.id.prev);
        nextPhoto = findViewById(R.id.next);
        placeImage = findViewById(R.id.place_image);
        String id = getIntent().getStringExtra("id");
        geoDataClient = Places.getGeoDataClient(this, null);


        nextPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPhoto();
            }
        });

        prevPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prevPhoto();
            }
        });
        getPhotoMetadata(id);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        placeImage.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    private void getPhotoMetadata(String placeId) {

        final Task<PlacePhotoMetadataResponse> photoResponse =
                geoDataClient.getPlacePhotos(placeId);

        photoResponse.addOnCompleteListener
                (new OnCompleteListener<PlacePhotoMetadataResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                        currentPhotoIndex = 0;
                        photosDataList = new ArrayList<>();
                        PlacePhotoMetadataResponse photos = task.getResult();
                        PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                        for(PlacePhotoMetadata photoMetadata : photoMetadataBuffer){
                            photosDataList.add(photoMetadataBuffer.get(0).freeze());
                        }

                        photoMetadataBuffer.release();

                        displayPhoto();
                    }
                });
    }
    private void getPhoto(PlacePhotoMetadata photoMetadata){
        Task<PlacePhotoResponse> photoResponse = geoDataClient.getPhoto(photoMetadata);
        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                PlacePhotoResponse photo = task.getResult();
                Bitmap photoBitmap = photo.getBitmap();

                placeImage.invalidate();
                placeImage.setImageBitmap(photoBitmap);
            }
        });
    }
    private void nextPhoto(){
        currentPhotoIndex++;
        displayPhoto();
    }
    private void prevPhoto(){
        currentPhotoIndex--;
        displayPhoto();
    }
    private void displayPhoto(){
        if(photosDataList.isEmpty() || currentPhotoIndex > photosDataList.size() - 1){
            return;
        }
        getPhoto(photosDataList.get(currentPhotoIndex));
        setButtonVisibility();
    }
    private void setButtonVisibility(){
        if(currentPhotoIndex == 0){
            prevPhoto.setEnabled(false);
            if(photosDataList.size() < 2){
                nextPhoto.setEnabled(false);
            }
        }else{
            prevPhoto.setEnabled(true);
            nextPhoto.setEnabled(true);
            if(currentPhotoIndex == photosDataList.size()-1){
                nextPhoto.setEnabled(false);
            }
        }
    }
}
