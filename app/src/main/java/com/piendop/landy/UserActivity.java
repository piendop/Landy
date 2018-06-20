package com.piendop.landy;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    /**GLOBAL VARIABLE**/
    static ArrayAdapter arrayAdapter;
    static ArrayList<String> places = new ArrayList<>();

    /***************PRESS BACK BUTTON*************************/

    @Override
    public void onBackPressed() {
        //super.onBackPressed();//prevent finish current activity automatically
        /*alert whether user want to log out*/
        /***Set up an alert***/
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Are you sure?")
                .setMessage("Do you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ParseUser.logOut();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    /**************MENU************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.user_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.logout){
            ParseUser.logOut();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    /********ON CREATE********/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initializePlaces();
        linkListViewWithData();
        /**DELETE PLACES*/
        deletePlaces();
    }

    /**DELETE PLACES*/
    private void deletePlaces() {
        ListView listView = findViewById(R.id.userPlaces);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int indexDeleted = i;
                new AlertDialog.Builder(UserActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Are you sure?")
                        .setMessage("Do you want to delete this place?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //remove places and locations
                                final String address = places.get(indexDeleted);
                                ParseQuery<ParseObject> query= ParseQuery.getQuery("Places");
                                query.whereEqualTo("userId",ParseUser.getCurrentUser().getObjectId());
                                query.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objects, ParseException e) {
                                        if(e==null&&objects.size()>0){
                                            for(ParseObject object:objects){
                                                if(object.getString("address").equals(address)){
                                                    object.deleteInBackground();
                                                    places.remove(indexDeleted);
                                                    //update changes in data in array adapter
                                                    arrayAdapter.notifyDataSetChanged();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No",null)
                        .show();
                return true;
            }
        });
    }

    /*******INITIALIZE DATA FOR ARRAY LIST PLACES TO DISPLAY**************/
    public void initializePlaces() {
        //clear to add new places
        places.removeAll(places);
        places.add("Add a new place...");
        //get current user
        /*ParseUser user = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> relation = user.getRelation("places");
        ParseQuery<ParseObject> query=relation.getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    if(objects.size()>0){
                        for(ParseObject object:objects){
                            //Log.i("Address",object.getString("address"));
                            //Log.i("Latitude",object.getString("latitudes"));
                            //Log.i("Longitude",object.getString("longitudes"));
                            places.add(object.getString("address"));
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });*/

        ParseQuery<ParseObject> query= ParseQuery.getQuery("Places");
        query.whereEqualTo("userId",ParseUser.getCurrentUser().getObjectId());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null&&objects.size()>0){
                    for(ParseObject object:objects){
                        places.add(object.getString("address"));
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    /**LINK LISTVIEW WITH DATA TO DISPLAY**/
    private void linkListViewWithData() {


        /**SET NAME FOR APP BAR BASED ON USERNAME*/
        setTitle(ParseUser.getCurrentUser().getUsername()+"'s Places");

        /**LIST VIEW*/
        ListView listView = findViewById(R.id.userPlaces);
        //set array adapter for list view
        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                R.layout.black_text,R.id.textView,places);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                if(i==0) {
                    intent.putExtra("index", Integer.toString(i));
                    startActivity(intent);
                }
                else{
                    final String address = places.get(i);
                    //get current user
                    /*ParseUser user = ParseUser.getCurrentUser();
                    ParseRelation<ParseObject> relation = user.getRelation("places");
                    ParseQuery<ParseObject> query=relation.getQuery();
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e==null){
                                if(objects.size()>0){
                                    String objectId="";
                                    for(ParseObject object:objects){
                                        if(object.getString("address").equals(address)){
                                            objectId=object.getObjectId();
                                            break;
                                        }
                                    }
                                    intent.putExtra("index",objectId);
                                    startActivity(intent);
                                }
                            }
                        }
                    });*/
                    ParseQuery<ParseObject> query= ParseQuery.getQuery("Places");
                    query.whereEqualTo("userId",ParseUser.getCurrentUser().getObjectId());
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {
                            if(e==null&&objects.size()>0){
                                String objectId="";
                                for(ParseObject object:objects){
                                    if(object.getString("address").equals(address)){
                                        objectId=object.getObjectId();
                                        break;
                                    }
                                }
                                intent.putExtra("index",objectId);
                                startActivity(intent);
                            }
                        }
                    });
                }

            }
        });
    }
}
