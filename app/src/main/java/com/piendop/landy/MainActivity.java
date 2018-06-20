package com.piendop.landy;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*******************************************************/
        initializeAppName();
        /**close virtual keyboard when enter**/
        closeVirtualKeyboardWhenEnter();
        /**close virtual keyboard when click somewhere*/
        closeVirtualKeyboardWhenClick();

        /**CHANGE SIGNUP TO LOGIN AND VICE VERSA*/
        changeState();

        /****IF USER ALREADY LOGGED IN THE APP SHOW USER LIST**/
        if(ParseUser.getCurrentUser()!=null){
            showUserActivity();
        }
        /************************************************************************************/
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    /*********************ON CLICK*********************/
    @Override
    public void onClick(View view) {
        //when click to change state
        if(view.getId()==R.id.changeStateText){
            TextView textView1 = findViewById(R.id.notifyChangeStateText);
            Button button = findViewById(R.id.button);
            if(((TextView)view).getText().toString().equals("Login")){
                ((TextView)view).setText("Sign up");
                textView1.setText("Don't have an account?");
                button.setText("Login");
            }else{
                ((TextView)view).setText("Login");
                textView1.setText("Already have an account?");
                button.setText("Sign up");
            }
        }else if(view.getId() == R.id.backgroundRelativeLayout ||//when click somewhere on the screen
                view.getId() == R.id.appName){
            InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if(inputMethodManager!=null)
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
        }
    }

    /**CHANGE SIGNUP TO LOGIN AND VICE VERSA*/
    private void changeState() {
        TextView changeStateText = findViewById(R.id.changeStateText);
        //listen when click to text view
        changeStateText.setOnClickListener(this);
    }

    /**close virtual keyboard when click somewhere*/
    public void closeVirtualKeyboardWhenClick() {
        RelativeLayout relativeLayout = findViewById(R.id.backgroundRelativeLayout);
        TextView appName =findViewById(R.id.appName);
        relativeLayout.setOnClickListener(this);
        appName.setOnClickListener(this);
    }

    /**close virtual keyboard when enter**/
    public void closeVirtualKeyboardWhenEnter() {
        (findViewById(R.id.passwordText)).setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i== KeyEvent.KEYCODE_ENTER && keyEvent.getAction()==KeyEvent.ACTION_DOWN){
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    if(inputMethodManager!=null)
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    getUserInfo(findViewById(R.id.button));
                }
                return false;
            }
        });
    }

    /********INITIALIZE APP NAME***************/
    private void initializeAppName() {
        //hide action bar
        Objects.requireNonNull(getSupportActionBar()).hide();
        //find textview app name by id
        TextView appName = findViewById(R.id.appName);
        //locate font for app name
        Typeface custom_font = Typeface.createFromAsset(getAssets(),  "fonts/grandhotel.ttf");
        //set font for app name
        appName.setTypeface(custom_font);
        //set text for app name
        appName.setText("LANDY");
    }

    /**METHOD OF BUTTON CLICK*/
    public void getUserInfo(View view) {
        //get username and password from edit text
        String username = ((EditText) findViewById(R.id.usernameText)).getText().toString();
        String password = ((EditText) findViewById(R.id.passwordText)).getText().toString();
        String state = ((Button)view).getText().toString();
        /**CHECK VALID INFORMATION*/
        checkValidInformation(username,password,state);
    }

    /**CHECK VALID INFORMATION*/
    private void checkValidInformation(String username, String password,String state)  {

        //check that user enter username/password or not
        if(username.isEmpty()||password.isEmpty()){
            Toast.makeText(MainActivity.this,"Empty username or password!",Toast.LENGTH_SHORT).show();
            return;
        }

        //check that if its loggin or signup state
        if(state.equals("Login")){
            checkValidLogin(username,password);
        }else{
            checkValidSignup(username,password);
        }
    }

    /**CHECK USER LOGIN CORRECT INFORMATION*/
    private void checkValidLogin(String username, String password)  {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(user!=null){
                    Log.i("Login","Successful");
                    /******START NEW ACTIVITY WHEN CLICK ON BUTTON SUCCESSFULLY*******/
                    //start new activity when login successfully
                    showUserActivity();

                }else{
                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**CHECK USER SIGNUP APPROPRIATE INFORMATION*/
    private void checkValidSignup(String username, String password)  {
        ParseUser parseUser = new ParseUser();

        parseUser.setUsername(username);
        parseUser.setPassword(password);

        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e==null){
                    Log.i("Sign up","Successful");
                    //start new activity when signup successfully
                    showUserActivity();
                }else{
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /******START NEW ACTIVITY WHEN CLICK ON BUTTON SUCCESSFULLY*******/
    private void showUserActivity() {

        Intent intent = new Intent(this,UserActivity.class);
        startActivity(intent);
    }
}
