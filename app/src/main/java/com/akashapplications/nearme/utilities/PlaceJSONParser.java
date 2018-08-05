package com.akashapplications.nearme.utilities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.akashapplications.nearme.MainActivity;
import com.akashapplications.nearme.models.PlaceModel;
import com.akashapplications.nearme.models.UserPhotoModel;
import com.akashapplications.nearme.services.LocationAddress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pratidhi on 3/8/18.
 */

public class PlaceJSONParser {
    JSONObject result;
    PlaceModel model = new PlaceModel();
    Context context;
    private static String address;

    public PlaceJSONParser(JSONObject result, Context context) {
        this.result = result;

        this.context = context;
    }

    public PlaceModel getPlaceModel()
    {


        try {
            JSONObject location = result.getJSONObject("geometry").getJSONObject("location");

            model.setLatitude(location.getDouble("lat"));
            model.setLongitude(location.getDouble("lng"));

//            LocationAddress locationAddress = new LocationAddress();
//            locationAddress.getAddressFromLocation(model.getLatitude(), model.getLongitude(),
//                   context , new PlaceJSONParser.GeocoderHandler());

            model.setId(result.getString("place_id"));
            model.setName(result.getString("name"));
            if(result.has("opening_hours") && result.getJSONObject("opening_hours").has("open_now"))
                model.setOpen(result.getJSONObject("opening_hours").getBoolean("open_now"));
            else
                model.setOpen(false);
            ArrayList<UserPhotoModel> photoList = new ArrayList<>();
            if(result.has("photos")) {
                JSONArray photoArray = result.getJSONArray("photos");
                for (int i = 0; i < photoArray.length(); i++) {
                    JSONObject photoObj = photoArray.getJSONObject(i);
                    UserPhotoModel m = new UserPhotoModel();
                    m.setPhotoReference(photoObj.getString("photo_reference"));
                    m.setHeight(photoObj.getInt("height") / 2);
                    m.setWidth(photoObj.getInt("width") / 2);
                    photoList.add(m);
                }
            }
            model.setPhotoList(photoList);

            if(result.has("rating"))
                model.setRating(result.getDouble("rating"));
            else
                model.setRating(0.0);

            if(result.has("vicinity"))
                model.setVicinity(result.getString("vicinity"));
            else
                model.setVicinity("Could not fetch address");
            model.setAddress(model.getVicinity());
            if(result.has("types"))
            {
                List<String> pTags = new ArrayList<>();
                JSONArray tagArray = result.getJSONArray("types");
                for(int x=0; x<tagArray.length();x++)
                    pTags.add(tagArray.getString(x));
                model.setCategoryTags(pTags);
            }
            else
                model.setCategoryTags(new ArrayList<String>(0));

//            Log.e("akx",model.getLatitude()+ " ");
//            Log.e("akx",model.getLongitude()+" ");
//            Log.e("akx",model.getId());
//            Log.e("akx",model.getName());
//            Log.e("akx",model.getAddress()+" ");
//            Log.e("akx",model.isOpen()+" ");
//            Log.e("akx",model.getPhotoList().get(0).getPhotoReference()+ " ");
//            Log.e("akx", (String.valueOf( model.getRating())));
//            Log.e("akx",model.getVicinity());
//            Log.e("akx",model.getCategoryTags().toString());
//            Log.e("akx","---------------------------------------------------------------------------------------------------------------------------");

        } catch (JSONException e) {
            Log.e("akx-error",e.getMessage());
        }

        return model;
    }

    public static class GeocoderHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    address = bundle.getString("address");
                    break;
                default:
                    address = "Could not fetch address";
            }


        }
    }
}
