package ctm.domain.com.catchthemonsters;


/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONObject;


public class Configs extends Application {


    // YOU CAN EDIT THIS AS YOU WISH, IT'S THE DISTANCE IN KILOMETERS FROM YOUR CURRENT LOCATION AND THE POINTS OF NEARBY INTERESTS
    public static double distanceFromCurrentLocation = 5;



    // YOU CAN CHANGE THIS ZOOM VALUE AS YOU WISH, IT'S FOR THE INITIAL ZOOM OF THE MAP IN THE HOME SCREEN
    public static float mapZoom = (float) 12.0;




    // IMPORTANT: Replace the red strings below with your own Application ID and Client Key of your app on https://back4app.com
    public static String PARSE_APP_ID = "0RwT0yB6EGbXqFiqvIzwYHQQjtxrzlb7Kz3Gtq3Y";
    public static String PARSE_CLIENT_KEY = "DOelGH9KvMJ8wNq5GH7pQFfzmtZIEL4LxaTt09KT";
    //------------------------------------------------------------------------------------






    /*************** DO NOT EDIT THE CODE BELOW *****************/

    public static String USER_CLASS_NAME = "_User";
    public static String USER_USERNAME = "username";
    public static String USER_AVATAR = "avatar";
    public static String USER_FULLNAME = "fullName";
    public static String USER_MONSTERS_CATCHED = "catched";
    public static String USER_POINTS = "points";
    public static String USER_CURRENT_LOCATION = "currentLocation";


    public static String MONSTERS_CLASS_NAME = "Monsters";
    public static String MONSTERS_MONSTER_NAME = "name";
    public static String MONSTERS_MONSTER_LOCATION = "location";
    public static String MONSTERS_MONSTER_POINTS = "points";

    public static String CATCHED_CLASS_NAME = "Catched";
    public static String CATCHED_USER_POINTER = "userPointer";
    public static String CATCHED_MONSTER_NAME = "monstName";
    public static String CATCHED_MONSTER_LOCATION = "monstLocation";
    public static String CATCHED_CREATED_AT = "createdAt";


    boolean isParseInitialized = false;


    public void onCreate() {
        super.onCreate();


        // Init Parse
        if (!isParseInitialized) {
            Parse.initialize(new Parse.Configuration.Builder(this)
                    .applicationId(String.valueOf(PARSE_APP_ID))
                    .clientKey(String.valueOf(PARSE_CLIENT_KEY))
                    .server("https://parseapi.back4app.com")
                    .build()
            );
            Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
            ParseUser.enableAutomaticUser();
            isParseInitialized = true;

            // Init Facebook Utils
            ParseFacebookUtils.initialize(this);
        }


    }// end oncreate()




    // MARK: - CUSTOM PROGRESS DIALOG -----------
    public static AlertDialog pd;
    public static void showPD(String mess, Context ctx) {
        AlertDialog.Builder db = new AlertDialog.Builder(ctx);
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.pd, null);
        TextView messTxt = dialogView.findViewById(R.id.pdMessTxt);
        messTxt.setText(mess);
        db.setView(dialogView);
        db.setCancelable(true);
        pd = db.create();
        pd.show();
    }
    public static void hidePD(){ pd.dismiss(); }



    // SIMPLE ALERT
    public static void simpleAlert(String mess, Context ctx) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
        alert.setMessage(mess)
            .setTitle(R.string.app_name)
            .setPositiveButton("OK", null)
            .setIcon(R.drawable.logo);
        alert.create().show();
    }



    // MARK: - SCALE BITMAP TO MAX SIZE
    public static Bitmap scaleBitmapToMaxSize(int maxSize, Bitmap bm) {
        int outWidth;
        int outHeight;
        int inWidth = bm.getWidth();
        int inHeight = bm.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, outWidth, outHeight, false);
        return resizedBitmap;
    }




}//@end


