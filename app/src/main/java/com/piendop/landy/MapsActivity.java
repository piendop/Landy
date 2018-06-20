package com.piendop.landy;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener
,GoogleMap.OnMarkerClickListener{

    /**GLOBAL VARIABLES**/
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    static String reviews = "";

    /***ON CREATE**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /***************PRESS BACK BUTTON*************************/

    @Override
    public void onBackPressed() {
        //super.onBackPressed();//prevent finish current activity automatically
        /*alert whether user want to log out*/
        /**initialize places*/
        Intent intent = new Intent(this,UserActivity.class);
        startActivity(intent);
    }

    /********ON MAP READY********/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //set long click save the place
        mMap.setOnMapLongClickListener(MapsActivity.this);
        mMap.setOnMarkerClickListener(this);
        Intent intent = getIntent();
        //if we change the location it will update for us
        final String index=intent.getStringExtra("index");
        if(index.equals("0")){
            //onLocationChanged when request location update by location manager
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //zoom the user location
                    centerUserLocation(location,"Your location");
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
            };

            /*THE FIRST TIME TIME TIME OR THE NEXT TIME OPEN THE APP*/
            //so for the first time open the app we don't click anything so it must show marker in the previous location
            //ask user to allow get location
            //version api<23
            //ask for permission at the first time
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //1 here is that we want to ask 1 time for the first time
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }else{
                //request location update if user allow u to access location
                startListening();
                //we r allowed to get access location when we open the app second time
                //startClick();
            }
        }else{//user click to see their favorite places
            //get current user
            ParseUser user = ParseUser.getCurrentUser();
            ParseRelation<ParseObject> relation = user.getRelation("places");
            ParseQuery<ParseObject> query=relation.getQuery();
            query.getInBackground(index, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if(e==null){
                        String address = object.getString("address");
                        double lat= Double.parseDouble(object.getString("latitudes"));
                        double lng=Double.parseDouble(object.getString("longitudes"));
                        LatLng location = new LatLng(lat,lng);
                        mMap.addMarker(new MarkerOptions().position(location).title(address)).setTag(index);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,10));
                    }
                }
            });
        }
    }

    /**ASK USER TO ACCEPT APP ACCESS USER LOCATION IN THE FIRST TIME**/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //we check self-permission again to make sure
            startListening();
        }
    }

    /**WHEN USERS SAY YES, REQUEST LOCATION UPDATE*/
    private void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //request location update will call onClickListener in onMapReady method
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        }
    }

    /***ZOOM USER LOCATION METHOD***/
    public void centerUserLocation(Location location, String address){
        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
        //clear markers
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title(address)).setTag("your location");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10));
    }

    /**LONG CLICK TO LOCATIONS AND SAVE THEM**/
    @Override
    public void onMapLongClick(LatLng latLng) {
        //remove location listener so that it cannot update to user
        // location when we add new places by click long marker
        locationManager.removeUpdates(locationListener);
        //get the address from click buttons
        String address = getAddress(latLng);
        ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> relation = user.getRelation("places");

        //save places to parse
        savePlacesToParse(address,latLng,relation);

        //finally save user
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Log.i("saveInBackgroundUser","successful");
                }else{
                    Log.i("saveInBackgroundUser","Failed. Error: "+e.toString());
                }
            }
        });
    }

    /***save places to parse object**/
    private void savePlacesToParse(final String address, final LatLng latLng,
                                   final ParseRelation<ParseObject> relation) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Places");
                query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if(e==null){
                    Log.i("FindInBackground","Retrieved " + objects.size()+" objects");
                    if(objects.size()>0){//having some objects in places class
                        boolean isExisted=false;
                        for(ParseObject object:objects){
                            //if we have a new location store to parse server
                            String existAddress = object.getString("address");
                            if(existAddress.equals(address)){
                                isExisted=true;
                            }
                        }
                        if(!isExisted){
                            final ParseObject newPlaces = new ParseObject("Places");
                            newPlaces.put("address",address);
                            newPlaces.put("latitudes",Double.toString(latLng.latitude));
                            newPlaces.put("longitudes",Double.toString(latLng.longitude));
                            newPlaces.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null){
                                        Log.i("saveInBackground","successful");
                                        //add marker of this clicked location
                                        mMap.addMarker(new MarkerOptions().position(latLng).
                                                title(address)).setTag(newPlaces.getObjectId());
                                        //notify location is saved
                                        Toast.makeText(MapsActivity.this,
                                                "Location Saved", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Log.i("saveInBackground","Failed. Error: "+e.toString());
                                    }
                                }
                            });
                            //then add relation
                            relation.add(newPlaces);
                        }
                    }else {//no objects in places class
                        ParseObject newPlaces = new ParseObject("Places");
                        newPlaces.put("address",address);
                        newPlaces.put("latitudes",Double.toString(latLng.latitude));
                        newPlaces.put("longitudes",Double.toString(latLng.longitude));

                        newPlaces.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null){
                                    Log.i("saveInBackground","successful");
                                }else{
                                    Log.i("saveInBackground","Failed. Error: "+e.toString());
                                }
                            }
                        });
                        //then add relation
                        relation.add(newPlaces);
                    }
                }
            }
        });
    }



    /**METHOD TO GET ADDRESS FROM LATITUDE AND LONGITUDE**/
    private String getAddress(LatLng latLng) {

        //create a geocoder to get the address of clicked location
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        //return address
        String addressInfo="";
        //create a list address to store addresses of clicked location
        try {//use try catch to check if its created or not
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            //check if we have address or not ==> check null and empty size
            if (addressList != null && addressList.size() > 0) {

                addressInfo=addressList.get(0).getAddressLine(0);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressInfo;
    }


    /**START NEW ACTIVITY TO REVIEW WHEN CLICK TO MARKER**/
    @Override
    public boolean onMarkerClick(Marker marker) {
        //get object id from address
        String id = marker.getTag().toString();
        if(id.equals("your location")){
            Toast.makeText(MapsActivity.this,"You cannot review your current location",
                    Toast.LENGTH_SHORT).show();
        }else{
            Intent intent = new Intent(MapsActivity.this,ReviewActivity.class);
            intent.putExtra("review",id);
            startActivity(intent);
        }
        return false;
    }
}
