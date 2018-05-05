package ctm.domain.com.catchthemonsters;

/*------------------------------------

    - Catch The Monsters -

    Created by cubycode @2017
    All Rights reserved

--------------------------------------*/


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Account extends AppCompatActivity {



    /* Variables */
    ParseUser currUser = ParseUser.getCurrentUser();
    MarshMallowPermission mmp = new MarshMallowPermission(this);




    @Override
    protected void onStart() {
        super.onStart();
        // Request Storage permission
        if(!mmp.checkPermissionForReadExternalStorage()) {
        mmp.requestPermissionForReadExternalStorage();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        // Init TabBar buttons
        Button tab_topten = (Button)findViewById(R.id.tab_topten);
        Button tab_nearby = (Button)findViewById(R.id.tab_nearby);

        tab_topten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, TopTen.class));
            }
        });

        tab_nearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, Nearby.class));
            }
        });



        // MARK: - SEE MONSTERS YOU'VE CATHED BUTTON
        Button seeMonstCatchedButt = (Button)findViewById(R.id.seeMonstButt);
        seeMonstCatchedButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Account.this, MonstersCatched.class));
            }
        });



        // Show User's data
        showUserData();

    }// end onCreate()






    // MARK: - SHOW USER'S DATA
    void showUserData() {

        // Set Title of the ActionBar
        getSupportActionBar().setTitle(currUser.getString(Configs.USER_FULLNAME));


        // Get Image
        final ImageView avImage = (ImageView) findViewById(R.id.avImage);
        ParseFile fileObject = (ParseFile)currUser.get(Configs.USER_AVATAR);
          if (fileObject != null ) {
             fileObject.getDataInBackground(new GetDataCallback() {
                 public void done(byte[] data, ParseException error) {
                     if (error == null) {
                         Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                         if (bmp != null) {
                             avImage.setImageBitmap(bmp);
             }}}});
        }

        // Get FullName
        EditText fnTxt = (EditText)findViewById(R.id.fnTxt);
        fnTxt.setText(currUser.getString(Configs.USER_FULLNAME));


        // Get stats
        TextView yourStatsTxt = (TextView)findViewById(R.id.yourStatsTxt);
        int catched = 0;
        int points = 0;
        if (currUser.getNumber(Configs.USER_MONSTERS_CATCHED) != null){ catched = (int) currUser.getNumber(Configs.USER_MONSTERS_CATCHED); }
        if (currUser.getNumber(Configs.USER_POINTS) != null){ points = (int) currUser.getNumber(Configs.USER_POINTS); }
        yourStatsTxt.setText("you've caught " + catched + " Monsters | made " + points + " points");







        // Set onClickListener to load an Avatar
        avImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert  = new AlertDialog.Builder(Account.this);
                alert.setTitle("SELECT SOURCE")
                .setIcon(R.drawable.logo)
                .setItems(new CharSequence[]
                {"Take a picture", "Pick from Gallery" },
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                // Open Camera
                                case 0:
                                    if (!mmp.checkPermissionForCamera()) {
                                        mmp.requestPermissionForCamera();
                                    } else { openCamera(); }
                                    break;

                                // Open Gallery
                                case 1:
                                    if (!mmp.checkPermissionForReadExternalStorage()) {
                                        mmp.requestPermissionForReadExternalStorage();
                                    } else {  openGallery();  }
                                    break;
                }}});
                alert.create().show();
        }});

    }








    // IMAGE HANDLING METHODS ------------------------------------------------------------------------
    int CAMERA = 0;
    int GALLERY = 1;
    Uri imageURI;
    File file;


    // OPEN CAMERA
    public void openCamera() {
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(), "image.jpg");
        imageURI = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageURI);
        startActivityForResult(intent, CAMERA);
    }


    // OPEN GALLERY
    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), GALLERY);
    }



    // IMAGE PICKED DELEGATE -----------------------------------
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Bitmap bm = null;

            // Image from Camera
            if (requestCode == CAMERA) {

                try {
                    File f = file;
                    ExifInterface exif = new ExifInterface(f.getPath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) { angle = 90; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) { angle = 180; }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) { angle = 270; }
                    Log.i("log-", "ORIENTATION: " + orientation);

                    Matrix mat = new Matrix();
                    mat.postRotate(angle);

                    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    bm = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                }
                catch (IOException | OutOfMemoryError e) { Log.i("log-", e.getMessage()); }


                // Image from Gallery
            } else if (requestCode == GALLERY) {
                try { bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                } catch (IOException e) { e.printStackTrace(); }
            }



            // Set image
            Bitmap scaledBm = Configs.scaleBitmapToMaxSize(300, bm);
            ImageView stImage = (ImageView)findViewById(R.id.avImage);
            stImage.setImageBitmap(scaledBm);
        }
    }
    //---------------------------------------------------------------------------------------------









    // MENU BUTTON ON ACTION BAR ----------------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            // MARK: - LOGOUT BUTTON
            case R.id.logoutButt:

                Configs.showPD("Logging out...", Account.this);

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        Configs.hidePD();

                        // Go Login activity
                        startActivity(new Intent(Account.this, Login.class));
                }});

                return true;



            // MARK: - UPDATE PROFILE BUTTON
            case R.id.saveButt:
                Configs.showPD("Updating your profile...", Account.this);

                EditText fnTxt = (EditText)findViewById(R.id.fnTxt);
                currUser.put(Configs.USER_FULLNAME, fnTxt.getText().toString());

                // Save image
                ImageView avImage = (ImageView) findViewById(R.id.avImage);
                Bitmap bitmap = ((BitmapDrawable) avImage.getDrawable()).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                ParseFile imageFile = new ParseFile("avatar.jpg", byteArray);
                currUser.put(Configs.USER_AVATAR, imageFile);


                // Saving block
                currUser.saveInBackground(new SaveCallback() {
                     @Override
                     public void done(ParseException error) {
                        if (error == null) {
                            Configs.hidePD();
                            Configs.simpleAlert("Your Profile has been updated!", Account.this);

                        // error
                        } else {
                            Configs.hidePD();
                            Configs.simpleAlert(error.getMessage(), Account.this);
                }}});
                return true;

        }
        return (super.onOptionsItemSelected(menuItem));
    }



}//@end
