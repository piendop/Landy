package com.piendop.landy;

import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CustomTextWatcher implements TextWatcher{

    String text="";
    String reviewId="";
    static int countUser=0;
    static String objectId;
    CustomTextWatcher(final String objectId){
        this.objectId=objectId;

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        text=charSequence.toString();
    }

    @Override
    public void afterTextChanged(Editable editable) {
        /*ParseUser user = ParseUser.getCurrentUser();
        Log.i("Username",user.getUsername());
        final ParseRelation<ParseObject> relationUser = user.getRelation("reviews");
        relationUser.getQuery().findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objectUsers, ParseException e) {
                if(e==null && objectUsers.size()>0){
                    for(ParseObject objectUser : objectUsers){
                        ++countUser;
                        final String userId = objectUser.getObjectId();
                        ParseQuery<ParseObject> query =  ParseQuery.getQuery("Places");
                        query.setLimit(1);
                        query.getInBackground(objectId, new GetCallback<ParseObject>() {
                            @Override
                            public void done(final ParseObject objectPlace, ParseException e) {
                                if(e==null && objectPlace!=null){
                                    final ParseRelation<ParseObject> relationPlace = objectPlace.getRelation("reviews");
                                    if(relationPlace==null){
                                        Log.i("Jump","Here");
                                    }
                                    relationPlace.getQuery().findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> objectPlaces, ParseException e) {
                                            if(e==null && objectPlaces.size()>0){
                                                for(ParseObject object:objectPlaces){
                                                    String placeId=object.getObjectId();
                                                    if(userId.equals(placeId)){
                                                        reviewId=userId;
                                                        break;
                                                    }
                                                }
                                                if(!reviewId.isEmpty()){//mean update review
                                                    ParseQuery<ParseObject> queryReview = ParseQuery.getQuery("Review");
                                                    queryReview.setLimit(1);
                                                    queryReview.getInBackground(reviewId, new GetCallback<ParseObject>() {
                                                        @Override
                                                        public void done(ParseObject object, ParseException e) {
                                                            if(e==null){
                                                                object.put("reviewText",text);
                                                                Log.i("Update","Successful");
                                                            }
                                                        }
                                                    });

                                                }else if(countUser>=objectUsers.size()){//means new review
                                                    ParseObject review = new ParseObject("Review");
                                                    review.put("reviewText",text);
                                                    review.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if(e==null){
                                                                Log.i("Create","Successful");
                                                            }
                                                        }
                                                    });
                                                    relationUser.add(review);
                                                    relationPlace.add(review);
                                                    objectPlace.saveInBackground(new SaveCallback() {
                                                        @Override
                                                        public void done(ParseException e) {
                                                            if(e==null){
                                                                Log.i("Save relation to Place","Successful");
                                                            }
                                                        }
                                                    });
                                                }
                                            }else{//no reviews in this place
                                                ParseObject review = new ParseObject("Review");
                                                review.put("reviewText",text);
                                                review.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if(e==null){
                                                            Log.i("Create","Successful");
                                                        }
                                                    }
                                                });
                                                relationUser.add(review);
                                                relationPlace.add(review);
                                                objectPlace.saveInBackground(new SaveCallback() {
                                                    @Override
                                                    public void done(ParseException e) {
                                                        if(e==null){
                                                            Log.i("Save relation to Place","Successful");
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });*/

        //get object user and its relation review
        /*ParseUser user = ParseUser.getCurrentUser();
        final ParseRelation<ParseObject> relationUser = user.getRelation("reviews");

        //get object place
        ParseQuery<ParseObject> query =  ParseQuery.getQuery("Places");
        query.setLimit(1);
        try {
            final ParseObject objectPlace = query.getFirst();
            if(objectPlace!=null) {
                //get relation reviews of object place
                final ParseRelation<ParseObject> relationPlace = objectPlace.getRelation("reviews");
                relationPlace.getQuery().findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objectPlaces, ParseException e) {
                        if (e == null && objectPlaces.size() > 0) {
                            for (ParseObject object : objectPlaces) {
                                ++countUser;
                                final String placeId = object.getObjectId();
                                //get User object to compare
                                relationUser.getQuery().findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> objectUsers, ParseException e) {
                                        for (ParseObject objectUser : objectUsers) {
                                            String userId = objectUser.getObjectId();
                                            if (userId.equals(placeId)) {//if there is one review id in two objects user and place break
                                                reviewId = userId;
                                                break;
                                            }
                                        }
                                        if (!reviewId.isEmpty()) {//mean update review
                                            ParseQuery<ParseObject> queryReview = ParseQuery.getQuery("Review");
                                            queryReview.setLimit(1);
                                            queryReview.getInBackground(reviewId, new GetCallback<ParseObject>() {
                                                @Override
                                                public void done(ParseObject object, ParseException e) {
                                                    if (e == null) {
                                                        object.put("reviewText", text);
                                                        Log.i("Update", "Successful");
                                                    }
                                                }
                                            });

                                        } else if (countUser >= 1) {//mean new review ==> create new object
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
                                            objectPlace.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        Log.i("Save relation to Place", "Successful");
                                                    }
                                                }
                                            });
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
                            objectPlace.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Log.i("Save relation to Place", "Successful");
                                    }
                                }
                            });
                        }
                    }

                });
            }

        }catch (ParseException e) {
            e.printStackTrace();
        }
        /*query.getInBackground(objectId, new GetCallback<ParseObject>() {
                    @Override
                    public void done(final ParseObject objectPlace, ParseException e) {
                        //get object place
                        if (e == null && objectPlace != null) {
                            final ParseRelation<ParseObject> relationPlace = objectPlace.getRelation("reviews");
                            relationPlace.getQuery().findInBackground(new FindCallback<ParseObject>() {
                                @Override
                                public void done(List<ParseObject> objectPlaces, ParseException e) {
                                    if (e == null && objectPlaces.size() > 0) {
                                        for (ParseObject object : objectPlaces) {
                                            ++countUser;
                                            final String placeId = object.getObjectId();
                                            //get User object to compare
                                            relationUser.getQuery().findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> objectUsers, ParseException e) {
                                                    for (ParseObject objectUser : objectUsers) {
                                                        String userId = objectUser.getObjectId();
                                                        if (userId.equals(placeId)) {//if there is one review id in two objects user and place break
                                                            reviewId = userId;
                                                            break;
                                                        }
                                                    }
                                                    if (!reviewId.isEmpty()) {//mean update review
                                                        ParseQuery<ParseObject> queryReview = ParseQuery.getQuery("Review");
                                                        queryReview.setLimit(1);
                                                        queryReview.getInBackground(reviewId, new GetCallback<ParseObject>() {
                                                            @Override
                                                            public void done(ParseObject object, ParseException e) {
                                                                if (e == null) {
                                                                    object.put("reviewText", text);
                                                                    Log.i("Update", "Successful");
                                                                }
                                                            }
                                                        });

                                                    } else if (countUser >= 1) {//mean new review ==> create new object
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
                                                        objectPlace.saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    Log.i("Save relation to Place", "Successful");
                                                                }
                                                            }
                                                        });
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
                                        objectPlace.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                if (e == null) {
                                                    Log.i("Save relation to Place", "Successful");
                                                }
                                            }
                                        });
                                    }
                                }

                            });

                        }
                    }
                });*/
        //finally save the relation
        /*user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Log.i("Save relation to user","Successful");
                }
            }
        });*/
    }

}
