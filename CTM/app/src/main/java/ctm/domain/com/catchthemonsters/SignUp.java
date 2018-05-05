package ctm.domain.com.catchthemonsters;

/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;


public class SignUp extends AppCompatActivity implements LocationListener {

    /* Views */
    EditText usernameTxt;
    EditText passwordTxt;
    EditText fullnameTxt;


    /* Variables */
    Location currentLocation;
    LocationManager locationManager;
    MarshMallowPermission mmp = new MarshMallowPermission(this);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Set Title on the ActionBar
        getSupportActionBar().setTitle("Sign Up");



        // Init views
        usernameTxt = (EditText)findViewById(R.id.usernameTxt2);
        passwordTxt = (EditText)findViewById(R.id.passwordTxt2);
        fullnameTxt = (EditText)findViewById(R.id.fullnameTxt);

        // Check for Location service
        if (!mmp.checkPermissionForLocation()) {
            mmp.requestPermissionForLocation();
        } else { getCurrentLocation(); }



            // SIGN UP BUTTON
            Button signupButt = (Button)findViewById(R.id.signUpButt2);
            signupButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Check current location one more time
                    getCurrentLocation();

                    if (usernameTxt.getText().toString().matches("") ||
                            passwordTxt.getText().toString().matches("") ||
                            fullnameTxt.getText().toString().matches("") )  {

                        Configs.simpleAlert("You must fill all the fields to Sign Up!", SignUp.this);


                    } else {
                        Configs.showPD("Please wait...", SignUp.this);
                        dismisskeyboard();

                        final ParseUser user = new ParseUser();
                        user.setUsername(usernameTxt.getText().toString());
                        user.setPassword(passwordTxt.getText().toString());

                        // Add FullName
                        user.put(Configs.USER_FULLNAME, fullnameTxt.getText().toString());

                        // Add current Location coordinates
                        if (currentLocation != null) {
                            ParseGeoPoint gp = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                            user.put(Configs.USER_CURRENT_LOCATION, gp);
                        }

                        // Saving block
                        user.signUpInBackground(new SignUpCallback() {
                            public void done(ParseException error) {
                                if (error == null) {

                                    // Save default avatar
                                    Bitmap bitmap = BitmapFactory.decodeResource(SignUp.this.getResources(), R.drawable.logo);
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                                    user.put(Configs.USER_AVATAR, imageFile);
                                    user.saveInBackground();

                                    Configs.hidePD();
                                    startActivity(new Intent(SignUp.this, Nearby.class));
                                } else {
                                    Configs.hidePD();
                                    Configs.simpleAlert(error.getMessage(), SignUp.this);
                        }}});
                    }

                }});


        }// end onCreate()






    // MARK: - GET CURRENT LOCATION ---------------------------------------------------------------
    protected void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {

        } else {locationManager.requestLocationUpdates(provider, 1000, 0, this); }
    }


    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);

        currentLocation = location;
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}






    // DISMISS KEYBOARD -------------------------------------------------
    public void dismisskeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(usernameTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(passwordTxt.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(fullnameTxt.getWindowToken(), 0);
    }





    // BACK BUTTON
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            // DEFAULT BACK BUTTON
            case android.R.id.home:
                this.finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }



}//@end




