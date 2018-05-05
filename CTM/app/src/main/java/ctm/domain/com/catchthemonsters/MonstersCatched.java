package ctm.domain.com.catchthemonsters;


/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MonstersCatched extends AppCompatActivity  {



    /* Variables */
    ParseObject catchedObj = null;
    List<ParseObject> cArray = null;
    List<Address> addresses = null;
    MarshMallowPermission mmp = new MarshMallowPermission(this);




    @Override
    protected void onStart() {
        super.onStart();

        // Call query
        queryCatched();
    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monsters_catched);
                super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Monsters Caught");



        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }//end onCreate()






    // MARK: - QUERY MONSTERS CATCHED ------------------------------------------------------------------
    void queryCatched() {
        Configs.showPD("Loading...", MonstersCatched.this);

        ParseQuery query = ParseQuery.getQuery(Configs.CATCHED_CLASS_NAME);
        query.whereEqualTo(Configs.CATCHED_USER_POINTER, ParseUser.getCurrentUser());
        query.orderByDescending(Configs.CATCHED_CREATED_AT);

        query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException error) {
                    if (error == null) {
                        cArray = objects;
                        Configs.hidePD();


                        // CUSTOM LIST ADAPTER
                        class ListAdapter extends BaseAdapter {
                            private Context context;

                            public ListAdapter(Context context, List<ParseObject> objects) {
                                super();
                                this.context = context;
                            }

                            // CONFIGURE CELL
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public View getView(int position, View cell, ViewGroup parent) {
                                if (cell == null) {
                                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    cell = inflater.inflate(R.layout.topten_cell, null);
                                }
                                // Get Parse object
                                catchedObj = cArray.get(position);

                                // Get Monster's name
                                TextView monstNameTxt = (TextView) cell.findViewById(R.id.fullnameTxt);
                                monstNameTxt.setText(catchedObj.getString(Configs.CATCHED_MONSTER_NAME));



                                // Get Where it's been cacthed
                                ParseGeoPoint gp = catchedObj.getParseGeoPoint(Configs.CATCHED_MONSTER_LOCATION);
                                try {
                                    Geocoder geocoder = new Geocoder(MonstersCatched.this, Locale.getDefault());
                                    double lat = gp.getLatitude();
                                    double lon = gp.getLongitude();

                                    addresses = geocoder.getFromLocation(lat, lon, 1);
                                    if (geocoder.isPresent()) {
                                        Address returnAddress = addresses.get(0);
                                        String address = returnAddress.getAddressLine(0);
                                        String city = returnAddress.getLocality();
                                        String state = returnAddress.getAdminArea();
                                        String country = returnAddress.getCountryName();
                                        String zipCode = returnAddress.getPostalCode();

                                        // Show City and Country
                                        TextView whereTxt = (TextView) cell.findViewById(R.id.statsTxt);
                                        whereTxt.setText("Caught in " + city + ", " + country );

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Geocoder not present!", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) { e.printStackTrace(); }



                                // Get Monster's Image
                                String mName = catchedObj.getString(Configs.CATCHED_MONSTER_NAME);
                                final int id = getResources().getIdentifier(mName.toLowerCase(), "drawable", getPackageName());
                                ImageView mImage = (ImageView) cell.findViewById(R.id.avatarImage);
                                mImage.setImageResource(id);


                                return cell;
                            }

                            @Override public int getCount() { return cArray.size(); }
                            @Override public Object getItem(int position) { return cArray.get(position); }
                            @Override public long getItemId(int position) { return position; }
                        }


                        // Init ListView and set its adapter
                        ListView catchedList = (ListView) findViewById(R.id.catchedListView);
                        catchedList.setAdapter(new ListAdapter(MonstersCatched.this, cArray));



                    // Error in query
                    } else {
                        Configs.hidePD();
                        Configs.simpleAlert(error.getMessage(), MonstersCatched.this);
                }}});

    }





    // MARK: - SHARE STATISTICS
    void shareStats() {
        Configs.hidePD();

        if (!mmp.checkPermissionForWriteExternalStorage()) {
            mmp.requestPermissionForWriteExternalStorage();

        } else {
            // Get currentUser
            ParseUser currUser = ParseUser.getCurrentUser();

            int catched = 0;
            int points = 0;
            if (currUser.getNumber(Configs.USER_MONSTERS_CATCHED) != null) {
                catched = (int) currUser.getNumber(Configs.USER_MONSTERS_CATCHED);
            }
            if (currUser.getNumber(Configs.USER_POINTS) != null) {
                points = (int) currUser.getNumber(Configs.USER_POINTS);
            }

            Bitmap bm = BitmapFactory.decodeResource(MonstersCatched.this.getResources(), R.drawable.logo);
            Uri uri = getImageUri(MonstersCatched.this, bm);
            Log.i("log-", "URI: " + uri);

            ShareCompat.IntentBuilder.from(this)
                    .setText("I've caught " + catched + " Monsters and earned " + points + " points on #CatchTheMonsters")
                    .setType("image/jpeg")
                    .setStream(uri)
                    .setChooserTitle("Share on...")
                    .startChooser();
        }

    }


    // Method to get URI of a stored image
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "image", null);
        return Uri.parse(path);
    }






    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.monst_catched_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            // DEFAULT BACK BUTTON
            case android.R.id.home:
                this.finish();
                return true;


            // SHARE BUTTON
            case R.id.shareButt:
                if (cArray.size() == 0) {
                    Configs.simpleAlert("You need to catch at least 1 Monster to share your Statistics!", MonstersCatched.this);

            } else { shareStats(); }

                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }


}//@end
