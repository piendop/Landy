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
import android.widget.EditText;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ReviewActivity extends AppCompatActivity {

    static boolean isReviewEmpty=false;
    static String reviewId="";
    static String text="";
    ParseUser user = ParseUser.getCurrentUser();
    //share preference for get data from storage
    //SharedPreferences sharedPreferences;


    @Override
    public void onBackPressed() {
        //finally save the relation
        getStupidText();
        doSomething();
        super.onBackPressed();
    }

    private void doSomething() {
    /*user.saveInBackground(new SaveCallback() {
        @Override
        public void done(ParseException e) {
            if(e==null){
                Log.i("Save relation to user","Successful");
            }
        }
    });*/
        Intent intent = getIntent();
        String objectId = intent.getStringExtra("review");

        Log.i("Review",text);

        //final String reviewText=sharedPreferences.getString("review","");
        //get object user and its relation review
        final ParseRelation<ParseObject> relationUser = user.getRelation("reviews");

        //get object place
        ParseQuery<ParseObject> query =  ParseQuery.getQuery("Places");
        query.setLimit(1);
        try {
            final ParseObject objectPlace = query.get(objectId);
            if(objectPlace!=null) {
                //get relation reviews of object place
                final ParseRelation<ParseObject> relationPlace = objectPlace.getRelation("reviews");
                relationPlace.getQuery().findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objectPlaces, ParseException e) {
                        if (e == null && objectPlaces.size() > 0) {
                            for (ParseObject object : objectPlaces) {
                                isReviewEmpty=true;
                                final String placeId = object.getObjectId();
                                //get User object to compare
                                relationUser.getQuery().findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objectUsers, ParseException e) {
                                        for (ParseObject objectUser : objectUsers) {
                                            String userId = objectUser.getObjectId();
                                            if (userId.equals(placeId)) {//if there is one review id in two objects user and place break
                                                reviewId = userId;
                                                //set text review to edit text
                                                /*EditText editText = findViewById(R.id.reviewEditText);
                                                //find the text in parse
                                                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Review");*/

                                                break;
                                            }
                                        }
                                        if (!reviewId.isEmpty()) {//mean update review
                                            reviewId="";
                                            ParseQuery<ParseObject> queryReview = ParseQuery.getQuery("Review");
                                            queryReview.setLimit(1);
                                            queryReview.getInBackground(reviewId, new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject object, ParseException e) {
                                                    if (e == null) {
                                                        object.put("reviewText", text);
                                                        Log.i("Update", "Successful");
                                                        object.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if(e==null){
                                                                    Log.i("Save update","Successful");
                                                                }
                                                            }
                                                        });
                                                        user.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if(e==null)
                                                                    Log.i("Save relation to user","Successful");
                                                                objectPlace.saveInBackground(new SaveCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if(e==null)
                                                                            Log.i("Save relation to place","Successful");
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        objectPlace.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if(e==null)
                                                                    Log.i("Save relation to place","Successful");
                                                            }
                                                        });
                                                        /*objectPlace.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    Log.i("Save relation to Place", "Successful");
                                                                }
                                                            }
                                                        });*/
                                                    }
                                                }
                                            });

                                        } else if (isReviewEmpty) {//mean new review ==> create new object
                                            ParseObject review = new ParseObject("Review");
                                            review.put("reviewText", text);
                                            review.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        Log.i("Create", "Successful");
                                                    }
                                                }
                                            });
                                            relationUser.add(review);
                                            relationPlace.add(review);
                                            user.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e==null)
                                                        Log.i("Save relation to user","Successful");
                                                    objectPlace.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if(e==null)
                                                                Log.i("Save relation to place","Successful");
                                                        }
                                                    });
                                                }
                                            });
                                            objectPlace.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e==null)
                                                        Log.i("Save relation to place","Successful");
                                                }
                                            });
                                            /*objectPlace.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        Log.i("Save relation to Place", "Successful");
                                                    }
                                                }
                                            });*/
                                            isReviewEmpty=false;//set up false again for a new activity
                                        }
                                    }
                                });
                            }
                        } else if (objectPlaces.size() == 0) {//no reviews in this place
                            ParseObject review = new ParseObject("Review");
                            review.put("reviewText", text);
                            review.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.i("Create", "Successful");
                                    }
                                }
                            });
                            relationUser.add(review);
                            relationPlace.add(review);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null)
                                        Log.i("Save relation to user","Successful");
                                    objectPlace.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e==null)
                                                Log.i("Save relation to place","Successful");
                                        }
                                    });
                                }
                            });
                            objectPlace.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null)
                                        Log.i("Save relation to place","Successful");
                                }
                            });
                            /*user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null){
                                        Log.i("Save relation to user","Successful");
                                        objectPlace.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if(e==null)
                                                    Log.i("Save relation to Place", "Successful");
                                            }
                                        });
                                    }
                                }
                            });*/
                            /*objectPlace.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.i("Save relation to user","Successful");
                                    }
                                }
                            });*/
                        }
                    }
                });

            }

        }catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        getStupidText();
        //doSomething();
    }

    private void getStupidText() {
        final EditText editText = findViewById(R.id.reviewEditText);
        //sharedPreferences = this.getApplicationContext().
        //      getSharedPreferences("com.piendop.landy", Context.MODE_PRIVATE);
        //sharedPreferences.edit().putString("review","No review in this place...").apply();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                final Timer timer = new Timer();
                timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timer.cancel();
                            }
                        });
                    }

                }, 1000);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                text=editText.getText().toString();
            }
        });
    }
}
