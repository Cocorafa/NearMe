package com.akashapplications.nearme.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.akashapplications.nearme.Adapters.StaggeredRecyclerViewAdapter;
import com.akashapplications.nearme.R;
import com.akashapplications.nearme.models.PlaceModel;
import com.akashapplications.nearme.utilities.CacheLocation;
import com.akashapplications.nearme.utilities.Constants;
import com.akashapplications.nearme.utilities.PlaceJSONParser;
import com.akashapplications.nearme.utilities.SearchJSONParser;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFrag extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    MaterialEditText searchInput;
    CacheLocation cacheLocation;
    ArrayList<PlaceModel> placeList = new ArrayList<>();
    RecyclerView recyclerView;
    private StaggeredRecyclerViewAdapter adapter;

    public SearchFrag() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        cacheLocation = new CacheLocation(getContext());
        searchInput = getView().findViewById(R.id.search_searchbar);
        searchInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    placeList.clear();
                    new LoadPlaces(searchInput.getText().toString().trim().replace(" ","%20")).execute();
                    return true;
                }
                return false;
            }
        });

        recyclerView = getView().findViewById(R.id.searchRecyclerView);
        adapter = new StaggeredRecyclerViewAdapter(getContext(),placeList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        adapter.notifyDataSetChanged();
        recyclerView.invalidate();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class LoadPlaces extends AsyncTask<String,String,String> {
        String input;

        public LoadPlaces(String input) {
            this.input = input;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            adapter.notifyDataSetChanged();
            recyclerView.invalidate();
        }

        @Override
        protected String doInBackground(String... strings) {
            String request = Constants.TEXT_SEARCH_API;
            request += input;
            request += "&locationbias=circle:"+cacheLocation.getRadius()+"@"+cacheLocation.getLat()+","+cacheLocation.getLng();
            Log.i("akx",request);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, request,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //If we are getting success from server
                            Log.i("akx",response);
                            try {
                                JSONObject object = new JSONObject(response);
                                JSONArray array = object.getJSONArray("candidates");
                                if(array.length() == 0)
                                {
                                    Toast.makeText(getContext(),object.getString("status"),Toast.LENGTH_LONG).show();
//                                    Toast.makeText(getContext(),"Sorry! There is no "+placeType+" nearby",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                for(int i = 0;i < array.length(); i++)
                                {
                                    placeList.add(new SearchJSONParser(array.getJSONObject(i),getContext()).getPlaceModel());
                                    adapter.notifyDataSetChanged();
                                }

                                Log.e("akx","count : "+placeList.size());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //You can handle error here if you want
                            Toast.makeText(getContext(),"Zero search result",Toast.LENGTH_LONG).show();
                            NetworkResponse networkResponse = error.networkResponse;
                            Log.i("akx", new String(error.networkResponse.data));
                            if(networkResponse != null && networkResponse.data != null)
                            {

                            }
                        }
                    }) {

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    //Adding parameters to request
                    return params;
                }
            };

            //Adding the string request to the queue
            stringRequest.setShouldCache(false);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            requestQueue.getCache().clear();
            requestQueue.add(stringRequest);
            return null;
        }
    }
}
