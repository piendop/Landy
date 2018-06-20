package com.piendop.landy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReviewActivity extends AppCompatActivity {

    static String text="";

    @Override
    public void onBackPressed() {
        doSomething();
        super.onBackPressed();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        //save to SharePreference
        final EditText editText = findViewById(R.id.reviewEditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                text=charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //then we must store a new text to shared preference
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences
                        ("com.piendop.landy",MODE_PRIVATE);
                sharedPreferences.edit().putString("review",text).apply();
            }
        });

        /****************************************************/
        /*******SHOW REVIEW IF IT ALREADY EXISTS********/
        Intent intent = getIntent();
        final String placeId = intent.getStringExtra("review");
        //find review object correspond to place and user
        ParseQuery<ParseObject> reviewQuery = ParseQuery.getQuery("Review");
        reviewQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){

                    if(objects.size()>0) {

                        for (ParseObject object : objects) {
                            if (object.getString("placeId").equals(placeId) &&
                                    object.getString("userId").equals(ParseUser.getCurrentUser().getObjectId())) {

                                ((EditText) findViewById(R.id.reviewEditText)).setText(object.getString("reviewText"));
                            }
                        }
                    }
                }
            }
        });
    }



    public void doSomething() {
        /****************************************************************/
        Intent intent = getIntent();
        final String placeId = intent.getStringExtra("review");
        //find review object correspond to place and user
        ParseQuery<ParseObject> reviewQuery = ParseQuery.getQuery("Review");
        reviewQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null){
                    //share preference for get data from storage
                    SharedPreferences sharedPreferences = getApplicationContext().
                            getSharedPreferences("com.piendop.landy",MODE_PRIVATE);
                    String reviewText = sharedPreferences.getString("review","");
                    if(objects.size()>0){
                        boolean isExisted=false;
                        for(ParseObject object:objects){
                            if(object.getString("placeId").equals(placeId) &&
                                    object.getString("userId").equals(ParseUser.getCurrentUser().getObjectId())){
                                isExisted=true;
                                //update review
                                object.put("reviewText",reviewText);
                                object.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e==null){
                                            Log.i("Update review","Successful");
                                        }
                                    }
                                });
                                break;
                            }
                        }
                        if(!isExisted){
                            Log.i("text",text);
                            ParseObject review = new ParseObject("Review");
                            review.put("reviewText",reviewText);
                            review.put("placeId",placeId);
                            review.put("userId",ParseUser.getCurrentUser().getObjectId());
                            review.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null){
                                        Log.i("Save new review","Successful");
                                    }
                                }
                            });
                        }
                    }else{//new review
                        Log.i("text",reviewText);
                        ParseObject review = new ParseObject("Review");
                        review.put("reviewText",reviewText);
                        review.put("placeId",placeId);
                        review.put("userId",ParseUser.getCurrentUser().getObjectId());
                        review.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e==null){
                                    Log.i("Save new review","Successful");
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}
