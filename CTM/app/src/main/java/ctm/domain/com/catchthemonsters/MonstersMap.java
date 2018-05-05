package ctm.domain.com.catchthemonsters;


/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.*;
import android.Manifest;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.DeleteCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class MonstersMap extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener {

    /* Views */
    private GoogleMap mapView;


    /* Variables */
    ArrayList<LatLng> markerPoints;
    Location currentLocation;
    LocationManager locationManager;
    ParseObject mObj;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monsters_map);

        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title on the ActionBar
        getSupportActionBar().setTitle("Monsters Map");

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Get objectID from previous .java
        Intent intent = getIntent();
        String objectID = intent.getStringExtra("objectID");
        mObj = ParseObject.createWithoutData(Configs.MONSTERS_CLASS_NAME, objectID);
        try {
            mObj.fetchIfNeeded().getParseObject(Configs.MONSTERS_CLASS_NAME);
            getCurrentLocation();

        } catch (ParseException e) { e.printStackTrace(); }



        // Initializing
        markerPoints = new ArrayList<LatLng>();




        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);


        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }// end onCreate()






    // MARK: - GET CURRENT LOCATION
    protected void getCurrentLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        currentLocation = locationManager.getLastKnownLocation(provider);

        if (currentLocation != null) {

        } else {
            locationManager.requestLocationUpdates(provider, 1000, 0, (android.location.LocationListener) this);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates((android.location.LocationListener) this);

        currentLocation = location;

    }







    // MARK: - SHOW CURRENT LOCATION ON MAP
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;

        // Enable MyLocation Layer of Google Map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapView.setMyLocationEnabled(true);
        mapView.setOnMarkerClickListener(this);


        // Get currentLocation
        getCurrentLocation();

        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        try {
            // Customise the styling of the base map using a JSON object defined in a raw resource file
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.black_map));

            if (!success) { Log.i("log-", "Style parsing failed."); }
        } catch (Resources.NotFoundException e) { Log.i("log-", "Can't find style. Error: ", e); }


        // Show the current location in Google Map
        mapView.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        // Zoom in the Google Map
        mapView.animateCamera(CameraUpdateFactory.zoomTo(Configs.mapZoom));
        // set Map type
        mapView.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        // Get Monster's name for the image
        String mName = mObj.getString(Configs.MONSTERS_MONSTER_NAME);
        final int imageID = getResources().getIdentifier(mName.toLowerCase(), "drawable", getPackageName());

        // Get Monsters' geoPoint
        ParseGeoPoint mGeoPoint = mObj.getParseGeoPoint(Configs.MONSTERS_MONSTER_LOCATION);

        // Add Monster on Map
        mapView.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(mName.toLowerCase(),100,100)))
                .title(mName)
                .snippet(mObj.getNumber(Configs.MONSTERS_MONSTER_POINTS) + " points")
                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                .position(new LatLng(mGeoPoint.getLatitude(), mGeoPoint.getLongitude())));
    }



    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }



    // MARK: - METHODS TO TRACE ROUTE ON THE MAP ------------------------------------------------------------
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {

        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // Invokes the thread for parsing the JSON data
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }


    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.CYAN);
            }


            // Finlly drawing polyline in the Google Map for the Route
            mapView.addPolyline(lineOptions);
            Configs.hidePD();

        }
    }
    // END ----------------------------------------------------------------------------------------------------










    // MARK: - TAP ON A MONSTER (A MAP'S MARKER) -----------------------------------
    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Get current Location
        getCurrentLocation();


        // Get geoPoint of the Monster
        ParseGeoPoint mGeoPoint = mObj.getParseGeoPoint(Configs.MONSTERS_MONSTER_LOCATION);
        final Location mLocation = new Location("");
        mLocation.setLatitude(mGeoPoint.getLatitude());
        mLocation.setLongitude(mGeoPoint.getLongitude());

        // EDIT THIS VALUE AS YOU WISH -> YOU HAVE TO GET AT LEAST 50 METERS CLOSE TO A MONSTER TO CATCH IT!
        final float radius = (float) 50.0;


        float distance = currentLocation.distanceTo(mLocation);

        /* test
        String clStr = String.valueOf(currentLocation.getLatitude());
        Toast.makeText(MonstersMap.this, clStr, Toast.LENGTH_SHORT).show();
        */



        // CHECK IF YOU'RE AROUND 50 METERS CLOSE TO THE MONSTER
        if (distance <= radius) {
            Configs.showPD("Catching " + marker.getTitle().toUpperCase() + "\n" + marker.getSnippet(), MonstersMap.this);


            // Save Catched Monster
            ParseObject cObj = new ParseObject(Configs.CATCHED_CLASS_NAME);
            cObj.put(Configs.CATCHED_USER_POINTER, ParseUser.getCurrentUser());
            cObj.put(Configs.CATCHED_MONSTER_NAME, mObj.getString(Configs.MONSTERS_MONSTER_NAME));
            cObj.put(Configs.CATCHED_MONSTER_LOCATION, mObj.getParseGeoPoint(Configs.MONSTERS_MONSTER_LOCATION));

            // Saving block
            cObj.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException error) {
                    if (error == null) {
                        Configs.hidePD();


                        // Update your statistics
                        ParseUser currUser = ParseUser.getCurrentUser();
                        currUser.increment(Configs.USER_MONSTERS_CATCHED, 1);
                        currUser.increment(Configs.USER_POINTS, mObj.getNumber(Configs.MONSTERS_MONSTER_POINTS));
                        try { currUser.save();
                        } catch (ParseException e) { e.printStackTrace(); }

                        // Then Remove the Monster from 'Monsters' class
                        mObj.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException error) {
                                if (error == null) {
                                    Configs.simpleAlert("You've caught " + marker.getTitle().toUpperCase() + "!\nGo back and search for new Monsters!", MonstersMap.this);

                                    // Lastly remove the Monster pin from map
                                    marker.remove();

                                // error
                                } else {
                                    Configs.simpleAlert(error.getMessage(), MonstersMap.this);
                        }}});

            }}});




            // YOU'RE TOO FAR AWAY FROM A MONSTER, CAN'T CATCH IT!
            } else {
                Configs.simpleAlert("You're too far away from " + marker.getTitle().toUpperCase() + "!\nGet closer to catch it!", MonstersMap.this);
            }



    return true;
    }










        // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.map_menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem menuItem) {
            switch (menuItem.getItemId()) {

                // DEFAULT BACK BUTTON
                case android.R.id.home:
                    this.finish();
                    return true;


                // MARK: - TRACE ROUTE BUTTON
                case R.id.traceRouteButt:
                    Configs.showPD("Tracing Route...", MonstersMap.this);

                    // Adding LatLng points to markerPoints array
                    ParseGeoPoint mGeoPoint = mObj.getParseGeoPoint(Configs.MONSTERS_MONSTER_LOCATION);
                    LatLng currPoint = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                    LatLng monstPoint = new LatLng(mGeoPoint.getLatitude(), mGeoPoint.getLongitude());

                    markerPoints.add(currPoint);
                    markerPoints.add(monstPoint);
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);
                    DownloadTask downloadTask = new DownloadTask();
                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);


                    return true;

            }
            return (super.onOptionsItemSelected(menuItem));
        }




}//@end
