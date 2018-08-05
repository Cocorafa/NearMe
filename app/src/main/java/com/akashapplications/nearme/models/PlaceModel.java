package com.akashapplications.nearme.models;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pratidhi on 3/8/18.
 */

public class PlaceModel {
    double latitude,longitude,rating;
    String id,name,vicinity,address;
    boolean isOpen;
    ArrayList<UserPhotoModel> photoList;
    List<String> categoryTags;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getCategoryTags() {
        return categoryTags;
    }

    public void setCategoryTags(List<String> categoryTags) {
        this.categoryTags = categoryTags;
    }

    public ArrayList<UserPhotoModel> getPhotoList() {
        return photoList;
    }

    public void setPhotoList(ArrayList<UserPhotoModel> photoList) {
        this.photoList = photoList;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
