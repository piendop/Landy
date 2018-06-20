package com.piendop.landy;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

        /************ASK TO TAKE A PICTURE********************/
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{//hoi roi thi choi luon
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent,1);
        }
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

                                /*******************************************************************************************/
                                /************SET IMAGE TO THE REVIEW**************/
                                ParseFile file  = (ParseFile) object.get("image");

                                if(!file.isDirty()){
                                    file.getDataInBackground(new GetDataCallback() {
                                        @Override
                                        public void done(byte[] data, ParseException e) {
                                            if(e==null && data!=null){
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length);

                                                LinearLayout linearLayout = findViewById(R.id.linearLayout);

                                                ImageView imageView = new ImageView(getApplicationContext());

                                                imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                                ));



                                                imageView.setImageBitmap(bitmap);

                                                linearLayout.addView(imageView);

                                            }
                                        }
                                    });
                                }
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

    /*************************************************************************************************************************/
    /**********************ACTION AFTER RECEIVE IMAGE***********************************/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1 ){
            if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode == RESULT_OK && data!=null){

            //uri link image with app
            Uri selectedImage  = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);

                Log.i("Photo","Received");
                /****UPLOAD PHOTO TO PARSE SERVER***/
                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);

                byte[] byteArray = stream.toByteArray();


                final ParseFile file = new ParseFile("image.png",byteArray);

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

                                        object.put("image",file);


                                        object.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if(e==null){
                                                    Toast.makeText(getApplicationContext(),"Image uploaded",Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(getApplicationContext(),"Image could not be uploaded - please try again later",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
