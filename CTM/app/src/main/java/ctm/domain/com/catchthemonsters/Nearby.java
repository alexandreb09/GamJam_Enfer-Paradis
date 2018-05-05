package ctm.domain.com.catchthemonsters;


/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import android.Manifest;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class Nearby extends AppCompatActivity implements LocationListener {



    /* Variables */
    Location currentLocation;
    LocationManager locationManager;
    List<ParseObject> mArray = null;

    MarshMallowPermission mmp = new MarshMallowPermission(this);




    // ON START() ------------------------------
    @Override
    protected void onStart() {
        super.onStart();

        // YOU'RE NOT LOGGED IN -> OPEN LOGIN
        ParseUser currUser = ParseUser.getCurrentUser();
        if (currUser.getUsername() == null) {
            startActivity(new Intent(Nearby.this, Login.class));


        // YOU'RE LOGGED IN -> GET CURRENT LOCATION
        } else {

            ParseInstallation installation = ParseInstallation.getCurrentInstallation();

            // IMPORTANT: Replace "478517440140" with your own GCM Sender ID
            installation.put("GCMSenderId", "478517440140");

            installation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    Log.i("log-", "REGISTERED FOR PUSH NOTIFICATIONS!");
            }});




            /* IMPORTANT: RUN THE APP ONLY ONCE, SIGN UP WITH A TEST USER AND WAIT FOR AN ALERT TO SHOW UP,
               IT WILL NOTIFY YOU THAT THE  "Monsters" CLASS AND DEMO MONSTER HAVE BEEN CREATED.
               THEN QUIT THE APP AND COMMENT THIS LINE OF CODE: */
            createMonstersClassAndData();




            // Get current location
            if (!mmp.checkPermissionForLocation()) {
                 mmp.requestPermissionForLocation();
            } else {
                getCurrentLocation();
            }
        }

    }


    // onCreate()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearby);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Nearby Monsters");




        // Init TabBar buttons
        Button tab_topten = (Button) findViewById(R.id.tab_topten);
        Button tab_account = (Button) findViewById(R.id.tab_account);

        tab_topten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Nearby.this, TopTen.class));
            }
        });

        tab_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Nearby.this, Account.class));
            }
        });


        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()






    // MARK: - GET CURRENT LOCATION -----------------------------------------
    protected void getCurrentLocation() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {
            // Call query
            queryNearbyMonsters(currentLocation);
            Log.i("log-", "currLocation: " + currentLocation.getLatitude() );

        } else {
            locationManager.requestLocationUpdates(provider, 1000, 0, this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        // Remove location callback:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(this);
        currentLocation = location;

        if (currentLocation != null) {
            Log.i("log-", "CURR LOC: " + currentLocation );

            // Call query
            queryNearbyMonsters(currentLocation);

        // NO GPS location found!
        } else { Configs.simpleAlert("Failed to get your Location.\nGo into Settings and make sure Location Service is enabled", Nearby.this); }
    }

    @Override public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override public void onProviderEnabled(String provider) { }
    @Override public void onProviderDisabled(String provider) { }








    // MARK: - QUERY NEARBY MONSTERS ---------------------------------------------------
    void queryNearbyMonsters(final Location location) {
        Configs.showPD("Searching Monsters...", Nearby.this);

        // Get geoPoint of your current location
        ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());


        ParseQuery query = ParseQuery.getQuery(Configs.MONSTERS_CLASS_NAME);
        query.whereWithinKilometers(Configs.MONSTERS_MONSTER_LOCATION, geoPoint, Configs.distanceFromCurrentLocation);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    mArray = objects;
                    Configs.hidePD();


                    // CUSTOM GRID ADAPTER
                    class GridAdapter extends BaseAdapter {
                        private Context context;

                        public GridAdapter(Context context, List<ParseObject> objects) {
                            super();
                            this.context = context;
                        }


                        // CONFIGURE CELL
                        @Override
                        public View getView(int position, View cell, ViewGroup parent) {
                            if (cell == null) {
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                cell = inflater.inflate(R.layout.nearby_cell, null);
                            }
                            // Get Parse object
                            ParseObject mObj = mArray.get(position);


                            // Get Monster name
                            TextView titleTxt = (TextView) cell.findViewById(R.id.mNameTxt);
                            int points = (int) mObj.getNumber(Configs.MONSTERS_MONSTER_POINTS);
                            titleTxt.setText(mObj.getString(Configs.MONSTERS_MONSTER_NAME) + " | " + points + " pts");


                            // Get Image
                            String mName = mObj.getString(Configs.MONSTERS_MONSTER_NAME);
                            final int id = getResources().getIdentifier(mName.toLowerCase(), "drawable", getPackageName());
                            ImageView mImage = (ImageView) cell.findViewById(R.id.mImage);
                            mImage.setImageResource(id);


                            // Get Distance from currentLocation in Km
                            ParseGeoPoint mGeoPoint = mObj.getParseGeoPoint(Configs.MONSTERS_MONSTER_LOCATION);

                            Location mLocation = new Location("");
                            mLocation.setLatitude(mGeoPoint.getLatitude());
                            mLocation.setLongitude(mGeoPoint.getLongitude());
                            float distInMeters = currentLocation.distanceTo(mLocation);
                            float distInKm = (float) (distInMeters /1000);

                            // Uncomment this line if you want to set distance in Miles
                            //float distInMiles = (float) (distInMeters * 0.000621371);

                            TextView distTxt = (TextView) cell.findViewById(R.id.distanceTxt);
                            distTxt.setText(String.format("%.2f", distInKm) + " Km");


                            return cell;
                        }

                        @Override public int getCount() { return mArray.size(); }
                        @Override public Object getItem(int position) { return mArray.get(position); }
                        @Override public long getItemId(int position) { return position; }
                    }


                    // Init GridView and set its adapter
                    GridView nearbyGrid = (GridView) findViewById(R.id.nearbyGridView);
                    nearbyGrid.setAdapter(new GridAdapter(Nearby.this, mArray));
                    nearbyGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            ParseObject obj = mArray.get(position);

                            // Pass obj to other Activity
                            Intent i = new Intent(Nearby.this, MonstersMap.class);
                            i.putExtra("objectID", obj.getObjectId());
                            startActivity(i);
                        }
                    });


                // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Nearby.this);
                }}});

    }










    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nearby_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // Refresh button
            case R.id.refreshButt:
                getCurrentLocation();
                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }









    // this method must run only once at first app startup
    void createMonstersClassAndData() {
        ParseObject mObj = new ParseObject(Configs.MONSTERS_CLASS_NAME);
        mObj.put(Configs.MONSTERS_MONSTER_NAME, "Goofy");
        mObj.put(Configs.MONSTERS_MONSTER_POINTS, 0);
        ParseGeoPoint loc = new ParseGeoPoint(0.0, 0.0);
        mObj.put(Configs.MONSTERS_MONSTER_LOCATION, loc);
        // Saving block
        mObj.saveInBackground(new SaveCallback() {
             @Override
             public void done(ParseException error) {
                if (error == null) {
                    Configs.hidePD();
                    Configs.simpleAlert("'Monsters' class and demo monster have been created!\nNow quit the app and follow the User Guide before running the app again!", Nearby.this);

                // error
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), Nearby.this);
        }}});
    }

}//@end
