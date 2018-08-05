package com.akashapplications.nearme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.akashapplications.nearme.PlaceImage;
import com.akashapplications.nearme.R;
import com.akashapplications.nearme.models.PlaceModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Created by pratidhi on 4/8/18.
 */

public class StaggeredRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PlaceModel> placeList;
    private View.OnClickListener defaultRequestBtnClickListener;

    public StaggeredRecyclerViewAdapter(Context context, ArrayList<PlaceModel> placeList) {
        this.context = context;
        this.placeList = placeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_listitem,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(placeList.size()==0)
            return;

        if(placeList.get(position).getPhotoList() != null && placeList.get(position).getPhotoList().size() >=0 ) {


            GeoDataClient mGeoDataClient = Places.getGeoDataClient(context, null);
            final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeList.get(position).getId());
            photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                    // Get the list of photos.
                    PlacePhotoMetadataResponse photos = task.getResult();
                    // Get the PlacePhotoMetadataBuffer (metadata for all of the photos).
                    PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                    // Get the first photo in the list.
                    if (photoMetadataBuffer != null && photoMetadataBuffer.getCount()>0) {
                        PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(0);

                    // Get the attribution text.
                    CharSequence attribution = photoMetadata.getAttributions();
                    // Get a full-size bitmap for the photo.
                    Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
                    photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                            PlacePhotoResponse photo = task.getResult();
                            Bitmap bitmap = photo.getBitmap();
                            holder.cell_image.setImageBitmap(bitmap);
                            holder.cell2_image.setImageBitmap(bitmap);
                        }
                    });
                }
            }
            });




//            Picasso.get()
//                    .load(placeList.get(position).getPhotoList().get(0).getPhotoReference())
//                    .placeholder(R.drawable.loading)
//                    .into(holder.cell_image);

//            Picasso.get()
//                    .load(placeList.get(position).getPhotoList().get(0).getPhotoReference())
//                    .placeholder(R.drawable.loading)
//                    .into(holder.cell2_image);

//            Log.e("akx",placeList.get(position).getPhotoList().get(0).getPhotoReference());
        }
        int openStatus = R.drawable.open;
        if (!placeList.get(position).isOpen())
            openStatus = R.drawable.closed;

        Glide.with(context)
                .load(openStatus)
                .into(holder.isOpenStatus);

        holder.cell_title.setText(placeList.get(position).getName());
        holder.cell2_title.setText(placeList.get(position).getName());

        holder.cell_ratings.setText(String.valueOf(placeList.get(position).getRating()));

        if(placeList.get(position).getCategoryTags()!=null && placeList.get(position).getCategoryTags().size()>0)
        {
            int length = placeList.get(position).getCategoryTags().size();
            String tags = "";
            for(int i=0;i<length;i++)
            {
                tags += placeList.get(position).getCategoryTags().get(i).replace("_"," ") + " ";
                if(i<length-1)
                    tags += "| ";
            }

            holder.tagLayout.setText(tags);
        }

        holder.cell2_address.setText(placeList.get(position).getAddress());


        holder.foreGround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.foreGround.setVisibility(View.GONE);
                holder.backGroound.setVisibility(View.VISIBLE);
            }
        });

        holder.backGroound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.foreGround.setVisibility(View.VISIBLE);
                holder.backGroound.setVisibility(View.GONE);
            }
        });

        holder.navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+placeList.get(position).getLatitude()+","+placeList.get(position).getLongitude()+"&mode=d");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);

            }
        });

        holder.images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, PlaceImage.class);
                i.putExtra("id",placeList.get(position).getId());
                i.putExtra("name",placeList.get(position).getName());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView cell_image,isOpenStatus,cell2_image;
        TextView cell_title,cell_ratings,cell2_title,cell2_address;
        TextView tagLayout;
        RelativeLayout foreGround;
        LinearLayout backGroound;
        FancyButton navigate,images;
        public ViewHolder(View itemView) {
            super(itemView);
            cell_image = itemView.findViewById(R.id.cell_image);
            cell_title = itemView.findViewById(R.id.cell_title);
            isOpenStatus = itemView.findViewById(R.id.cell_isopen_status);
            cell_ratings = itemView.findViewById(R.id.cell_rating);
            cell2_image = itemView.findViewById(R.id.cell2_image);
            cell2_title = itemView.findViewById(R.id.cell2_title);
            cell2_address = itemView.findViewById(R.id.cell2_address);
            tagLayout = itemView.findViewById(R.id.cell2_tagContainer);
            foreGround = itemView.findViewById(R.id.card_foreground);
            backGroound = itemView.findViewById(R.id.card_background);
            navigate = itemView.findViewById(R.id.cell2_navigate);
            images = itemView.findViewById(R.id.cell2_photos);


        }
    }
}
