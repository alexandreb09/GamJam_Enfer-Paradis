package ctm.domain.com.catchthemonsters;

/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class TopTen extends AppCompatActivity {


    /* Variables */
    List<ParseObject> usersArray = null;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_ten);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set Title of the ActionBar
        getSupportActionBar().setTitle("Top 10");




        // Init TabBar buttons
        Button tab_nearby = (Button)findViewById(R.id.tab_nearby);
        Button tab_account = (Button)findViewById(R.id.tab_account);

        tab_nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TopTen.this, Nearby.class));
            }
        });

        tab_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TopTen.this, Account.class));
            }
        });


        // Init AdMob banner
        AdView mAdView = (AdView) findViewById(R.id.admobBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        // Call query
        queryTopTen();

    }// endOnCreate()




    // MARK: - QUERY TOP 10
    void queryTopTen() {
        Configs.showPD("Loading Top 10...", TopTen.this);

        ParseQuery query = ParseQuery.getQuery(Configs.USER_CLASS_NAME);
        query.setLimit(10);
        query.orderByDescending(Configs.USER_MONSTERS_CATCHED);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException error) {
                if (error == null) {
                    usersArray = objects;
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
                            ParseObject uObj = usersArray.get(position);

                            TextView fnTxt = (TextView) cell.findViewById(R.id.fullnameTxt);
                            fnTxt.setText(uObj.getString(Configs.USER_FULLNAME));

                            // Get Stats
                            TextView statsTxt = (TextView) cell.findViewById(R.id.statsTxt);
                            int catched = 0;
                            int points = 0;
                            if (uObj.getNumber(Configs.USER_MONSTERS_CATCHED) != null){ catched = (int) uObj.getNumber(Configs.USER_MONSTERS_CATCHED); }
                            if (uObj.getNumber(Configs.USER_POINTS) != null){ points = (int) uObj.getNumber(Configs.USER_POINTS); }
                            statsTxt.setText("has caught " + catched + " Monster | made " + points + " points");


                            // Get Image
                            final ImageView avatarImg = (ImageView) cell.findViewById(R.id.avatarImage);
                            avatarImg.setImageResource(R.drawable.logo);
                            ParseFile fileObject = (ParseFile)uObj.get(Configs.USER_AVATAR);
                            if (fileObject != null ) {
                                fileObject.getDataInBackground(new GetDataCallback() {
                                    public void done(byte[] data, ParseException error) {
                                        if (error == null) {
                                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                                            if (bmp != null) {
                                                avatarImg.setImageBitmap(bmp);
                                }}}});
                            }


                            return cell;
                        }

                        @Override
                        public int getCount() { return usersArray.size(); }

                        @Override
                        public Object getItem(int position) { return usersArray.get(position); }

                        @Override
                        public long getItemId(int position) { return position; }
                    }


                    // Init ListView and set its adapter
                    ListView storesList = (ListView) findViewById(R.id.toptenListView);
                    storesList.setAdapter(new ListAdapter(TopTen.this, usersArray));


                    // Error in query
                } else {
                    Configs.hidePD();
                    Configs.simpleAlert(error.getMessage(), TopTen.this);
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
                queryTopTen();
                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }


}//@end
