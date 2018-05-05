package ctm.domain.com.catchthemonsters;


/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
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
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Login extends AppCompatActivity implements LocationListener {

    /* Views */
    EditText usernameTxt;
    EditText passwordTxt;


    /* Variables */
    Location currentLocation;
    LocationManager locationManager;
    MarshMallowPermission mmp = new MarshMallowPermission(this);


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.login);
            super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


            // Set Title on the ActionBar
            getSupportActionBar().setTitle("Login");



            usernameTxt = (EditText)findViewById(R.id.usernameTxt);
            passwordTxt = (EditText)findViewById(R.id.passwordTxt);

            // Check for Location service
            if (!mmp.checkPermissionForLocation()) {
                mmp.requestPermissionForLocation();
            } else { getCurrentLocation(); }




            // MARK: - LOGIN BUTTON ------------------------------------------------------------------------
            Button loginButt = (Button)findViewById(R.id.loginButt);
            loginButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Configs.showPD("Please wait...", Login.this);

                    ParseUser.logInInBackground(usernameTxt.getText().toString(), passwordTxt.getText().toString(),
                            new LogInCallback() {
                                public void done(ParseUser user, ParseException error) {
                                    if (user != null) {
                                        Configs.hidePD();
                                        startActivity(new Intent(Login.this, Nearby.class));
                                    } else {
                                        Configs.hidePD();
                                        Configs.simpleAlert(error.getMessage(), Login.this);
                                    }
                                }
                            });

                }
            });




            // MARK: - SIGN UP BUTTON ---------------------------------------------------------------
            Button signupButt = (Button)findViewById(R.id.signUpButt);
            signupButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Login.this, SignUp.class));
                }
            });







            // MARK: - FACEBOOK LOGIN BUTTON ------------------------------------------------------------------
            Button fbButt = (Button)findViewById(R.id.facebookButt);
            fbButt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> permissions = Arrays.asList("public_profile", "email");
                    Configs.showPD("Please wait...", Login.this);

                    ParseFacebookUtils.logInWithReadPermissionsInBackground(Login.this, permissions, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user == null) {
                                Log.i("log-", "Uh oh. The user cancelled the Facebook login.");
                                Configs.hidePD();

                            } else if (user.isNew()) {
                                getUserDetailsFromFB();

                            } else {
                                Log.i("log-", "RETURNING User logged in through Facebook!");
                                Configs.hidePD();
                                startActivity(new Intent(Login.this, Nearby.class));
                            }}});
                }});




            // This code generates a KeyHash that you'll have to copy from your Logcat console and paste it into Key Hashes field in the 'Settings' section of your Facebook Android App
            try {
                PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.i("log-", "keyhash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (PackageManager.NameNotFoundException e) {
            } catch (NoSuchAlgorithmException e) {}



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
        } else { locationManager.requestLocationUpdates(provider, 1000, 0, this); }
    }


    @Override
    public void onLocationChanged(Location location) {
        // remove location callback:
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        currentLocation = location;
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override public void onProviderEnabled(String provider) {}
    @Override public void onProviderDisabled(String provider) {}






    // MARK: - FACEBOOK GRAPH REQUEST --------------------------------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }


    void getUserDetailsFromFB() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),new GraphRequest.GraphJSONObjectCallback(){
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                String facebookID = "";
                String name = "";
                String username = "";

                try{
                    name = object.getString("name");
                    facebookID = object.getString("id");

                    String[] one = name.toLowerCase().split(" ");
                    for (String word : one) { username += word; }
                    Log.i("log-", "USERNAME: " + username + "\n");
                    Log.i("log-", "name: " + name + "\n");

                } catch(JSONException e){ e.printStackTrace(); }


                // SAVE NEW USER IN YOUR PARSE DASHBOARD -> USER CLASS
                final String finalFacebookID = facebookID;
                final String finalUsername = username;
                final String finalName = name;

                final ParseUser currUser = ParseUser.getCurrentUser();
                currUser.put(Configs.USER_USERNAME, finalUsername);
                currUser.put(Configs.USER_FULLNAME, finalName);

                // Add current Location coordinates
                if (currentLocation != null) {
                    ParseGeoPoint gp = new ParseGeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    currUser.put(Configs.USER_CURRENT_LOCATION, gp);
                }

                currUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Configs.hidePD();

                        startActivity(new Intent(Login.this, Nearby.class));
                        Log.i("log-", "NEW USER signed up and logged in through Facebook!\n");


                        // Get and Save avatar from Facebook
                        new Timer().schedule(new TimerTask() {
                            @Override public void run() {
                                try {
                                    URL imageURL = new URL("https://graph.facebook.com/" + finalFacebookID + "/picture?type=large");
                                    Bitmap avatarBm = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    avatarBm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    ParseFile imageFile = new ParseFile("image.jpg", byteArray);
                                    currUser.put(Configs.USER_AVATAR, imageFile);
                                    currUser.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException error) {
                                            Log.i("log-", "AVATAR SAVED!");
                                        }});
                                } catch (IOException error) { error.printStackTrace(); }

                            }}, 1000); // 1 second


                    }}); // end saveInBackground

            }}); // end graphRequest


        Bundle parameters = new Bundle();
        parameters.putString("fields", "name, picture.type(large)");
        request.setParameters(parameters);
        request.executeAsync();
    }
    // END FACEBOOK GRAPH REQUEST --------------------------------------------------------------------




}//@end

