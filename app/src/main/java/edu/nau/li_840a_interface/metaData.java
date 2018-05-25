/*
 *  Author: Andrew Greene
 *  Last updated: April 22th, 2018
 *  Description: Here the user enters in all of the Relative MetaData pertaining to the data set.
 *               The data is saved and passed to the next screen(s).  Background Operations are
 *               Time, Date, and GPS Autofill, and field validation
 */

package edu.nau.li_840a_interface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AuthProvider;
import java.security.spec.ECField;
import java.text.DateFormat;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;


public class metaData extends AppCompatActivity {
    //Shared Preference Keys
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String K_Name = "nameKey";
    public static final String K_Site = "siteKey";
    public static final String K_ID = "idKey";
    public static final String K_Temp = "tempKey";
    public static final String K_Comments = "commentsKey";
    public static final String K_Long = "longKey";
    public static final String K_Lat = "latKey";
    public static final String K_Elev = "elevKey";

    SharedPreferences sharedpreferences;
    // Initializing Fields
    public EditText OperatorName;
    public EditText SiteName;
    public EditText sampleID;
    public EditText temperature;
    public EditText comments;
    Intent metaDataScreen;
    // Camera Variables
    ImageButton cameraB;
    public Bitmap Bimage;
    public Bitmap image;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    public ImageView imagePreview;

    // Time/Date Variables
    private String currentDateTimeString;
    private String currentDateTimeFormatted;

    //GPS Variables
    private Button GPS_b;
    public EditText et_GPSLong;
    public EditText et_GPSLat;
    public EditText et_Elevation;
    private LocationManager locationManager;
    private LocationListener listener;

    String mCurrentPhotoPath;

    // Text Watcher Handles Field Validation, checks after the Text has been changed, Raises Toast
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            checkFieldsForEmptyValues();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.meta_data);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        getWindow().getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                    }
                });

        // Assigning Variables by ID
        Date currentDate = new Date();
        OperatorName = findViewById(R.id.et_ON);
        SiteName = findViewById(R.id.et_SN);
        sampleID = findViewById(R.id.et_SID);
        temperature = findViewById(R.id.et_Temp);
        comments = findViewById(R.id.et_Com);
        imagePreview = findViewById(R.id.imageView);

        // Initiates field Validation, Disables "Finish" button on Screen Creation
        SiteName.addTextChangedListener(textWatcher);
        sampleID.addTextChangedListener(textWatcher);
        checkFieldsForEmptyValues();


        //***Time and Date***
        TextView tv_Date;
        currentDateTimeString = DateFormat.getDateTimeInstance().format(currentDate);

        // Set time on screen
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        tv_Date = findViewById(R.id.tv_Date);
        tv_Date.setText(currentDateTimeString);

        // Format time to filename
        DateFormat dateAndTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        currentDateTimeFormatted = dateAndTimeFormat.format(currentDate);

        //***Camera***
        ImageButton imageB = (ImageButton) findViewById(R.id.cameraB);

        //***GPS***
        et_GPSLong = findViewById(R.id.et_GPSLong);
        et_GPSLat = findViewById(R.id.et_GPSLat);
        et_Elevation = findViewById(R.id.et_Elevation);

        GPS_b = findViewById(R.id.GPSbutton);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // GPS Listener Initiates GPS Search on Create.
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                et_GPSLong.setText("" + location.getLongitude());
                et_GPSLat.setText("" + location.getLatitude());
                et_Elevation.setText("" + location.getAltitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        configure_button();
        GPS_b.performClick();

        // ***AutoFill***

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.contains("nameKey")) {
            OperatorName.setText(sharedpreferences.getString("nameKey", null));
        }
        if (sharedpreferences.contains("siteKey")) {
            SiteName.setText(sharedpreferences.getString("siteKey", null));
        }
        //if(sharedpreferences.contains("idKey")){
        //    sampleID.setText(sharedpreferences.getString("idKey",null));
        //}

        String CheckFlag;
        CheckFlag = getIntent().getStringExtra("FLAG");
        //Toast.makeText(metaData.this,CheckFlag,Toast.LENGTH_LONG).show();
        if (CheckFlag.equals("True")) {
            //Toast.makeText(metaData.this,"I am in the right spot",Toast.LENGTH_LONG).show();
            if (sharedpreferences.contains("idKey")) {
                sampleID.setText(sharedpreferences.getString("idKey", null));
            }
            if (sharedpreferences.contains("tempKey")) {
                temperature.setText(sharedpreferences.getString("tempKey", null));
            }
            if (sharedpreferences.contains("commentsKey")) {
                comments.setText(sharedpreferences.getString("commentsKey", null));
            }
            if (sharedpreferences.contains("longKey")) {
                et_GPSLong.setText(sharedpreferences.getString("longKey", null));
            }
            if (sharedpreferences.contains("latKey")) {
                et_GPSLat.setText(sharedpreferences.getString("latKey", null));
            }
            if (sharedpreferences.contains("elevKey")) {
                et_Elevation.setText(sharedpreferences.getString("elevKey", null));
            }
            Bitmap TempImage;
            try{
                TempImage = getIntent().getParcelableExtra("IMAGE");
                image = TempImage;
                imagePreview.setImageBitmap(image);
            } catch( Exception e){}
        }

    }

    // Requesting Permissions for GPS
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }


    // Starts On click Listener for GPS
    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        GPS_b.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                // Lint Error Suppressed,  Always checking for permission on Home Screen
                // (noinspection MissingPermission)
                locationManager.requestLocationUpdates("gps", 5000, 0, listener);
            }
        });
    }

    //***Camera***
    private File createImageFile() throws IOException {
        //String timeStamp = currentDateTimeFormatted;
        //String imageFileName = "metaSite + "_" + metaSampleId + "_" + metaTime";

        // Create an image file name
        // File name is redundant, since File.createTempFile assigns a random number string to every File
        String imageFileName = "image";
        //String imageFileName = "I-" + SiteName.getText() + "_" + sampleID.getText() + "_" + currentDateTimeFormatted;
        //System.out.println("TEST: ImageName: "+ imageFileName);
        //Toast.makeText(metaData.this,imageFileName,Toast.LENGTH_LONG).show();

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                System.out.println("TEST: Error occurred while creating the File");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "edu.nau.li_840a_interface", photoFile);
                System.out.println("TEST: Camera intent: SUCCESS");
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            } else {
                System.out.println("TEST: Writing PhotoFile Failed");
            }

        }
    }

    //This just sets the BitMap to the Image View.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //System.out.println("TEST: Path: " + storageDir);
            //String imageFileName = "I-" + SiteName.getText() + "_" + sampleID.getText() + "_" + currentDateTimeFormatted;
            //System.out.println("TEST: File: " + imageFileName);
            Bimage = BitmapFactory.decodeFile(mCurrentPhotoPath);
            MediaStore.Images.Media.insertImage(getContentResolver(), Bimage, mCurrentPhotoPath, "Site Name: " + SiteName.getText() + "Sample ID:" + sampleID.getText());
            //Toast.makeText(metaData.this,"Saved to Gallery",Toast.LENGTH_LONG).show();
            setPic();
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = imagePreview.getWidth();
        int targetH = imagePreview.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        //Toast.makeText(metaData.this,"Width:"+String.valueOf(photoW),Toast.LENGTH_LONG).show();
        int photoH = bmOptions.outHeight;
        // Handling Error so it only accepts Portrait Images
        // If statement accepts Portrait Images Images
        if (photoW < photoH) {

            // Determine how much to scale down the image

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            image = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            //Toast.makeText(metaData.this, "Unscaled: " + image.getByteCount(), Toast.LENGTH_SHORT).show();
            //scaleImage();
            //Toast.makeText(metaData.this, "Scaled: " + image.getByteCount(), Toast.LENGTH_SHORT).show();
            imagePreview.setImageBitmap(image);


        } else {
            Toast.makeText(metaData.this, "Please take a Portrait Picture", Toast.LENGTH_LONG).show();
        }


    }

    public void scaleImage() {
        if(image.getByteCount() > 500000) {
            Bitmap original = BitmapFactory.decodeFile(mCurrentPhotoPath);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            original.compress(Bitmap.CompressFormat.PNG, 90, out);
            image = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
            Toast.makeText(metaData.this, "Re-Scaling....", Toast.LENGTH_SHORT).show();
            scaleImage();
        }
    }

    //***Field Validation***
    private  void checkFieldsForEmptyValues(){
        SiteName = findViewById(R.id.et_SN);
        OperatorName = findViewById(R.id.et_ON);
        sampleID = findViewById(R.id.et_SID);
        temperature = findViewById(R.id.et_Temp);
        comments = findViewById(R.id.et_Com);
        Button validate = (Button) findViewById(R.id.b_finish);

        String s1 = OperatorName.getText().toString();
        String s2 = SiteName.getText().toString();
        String s3 = sampleID.getText().toString();
        String s4 = temperature.getText().toString();
        String s5 = comments.getText().toString();

        if(s1.equals("") || s2.equals("") || s3.equals(""))
        {
            validate.setEnabled(false);
        }
        else if(s2.contains(" ")){
            Toast toast1 = Toast.makeText(getApplicationContext(), "Site Name cannot use spaces", Toast.LENGTH_SHORT);
            toast1.show();
            validate.setEnabled(false);
        }
        else if(s3.contains(" ")){
            Toast toast2 = Toast.makeText(getApplicationContext(), "Sample ID cannot use spaces", Toast.LENGTH_SHORT);
            toast2.show();
            validate.setEnabled(false);
        }

        else
        {
            validate.setEnabled(true);
        }
    }

    // Disables Back Button
    @Override
    public void onBackPressed()
    {

    }


    // "FINISH" Button, goes to the next screen to start Data Collection
    public void goGraphScreen(View view)
    {
        String[] passingValues = new String[8];
        int counter;
        Intent graphScreen;

        //Shared Preference Loading.
        String n  = OperatorName.getText().toString();
        String s  = SiteName.getText().toString();
        String a  = sampleID.getText().toString();
        String b  = temperature.getText().toString();
        String c  = comments.getText().toString();
        String d  = et_GPSLong.getText().toString();
        String e  = et_GPSLat.getText().toString();
        String f  = et_Elevation.getText().toString();

        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString(K_Name, n);
        editor.putString(K_Site, s);
        editor.putString(K_ID, a);
        editor.putString(K_Temp, b);
        editor.putString(K_Comments, c);
        editor.putString(K_Long, d);
        editor.putString(K_Lat, e);
        editor.putString(K_Elev, f);
        editor.apply();
        Toast.makeText(metaData.this,"Saved",Toast.LENGTH_LONG).show();

        // Get each string value from the text boxes and place them in an array
        // TODO Might be redundant, try using the strings above
        passingValues[0] = SiteName.getText().toString();
        passingValues[1] = OperatorName.getText().toString();
        passingValues[2] = sampleID.getText().toString();
        passingValues[3] = temperature.getText().toString();
        passingValues[4] = comments.getText().toString();
        passingValues[5] = et_GPSLong.getText().toString();
        passingValues[6] = et_GPSLat.getText().toString();
        passingValues[7] = et_Elevation.getText().toString();

        // Check each value in the array for null or empty strings
        for (counter = 0; counter < passingValues.length; counter++)
        {

            // If one is a null value or empty string, set it to the string "NULL"
            if (passingValues[counter] == null || passingValues[counter].equals(""))
            {
                passingValues[counter] = "NA";
            }

        }

        // Initialize the graph screen
        graphScreen = new Intent(this, graphScreen.class);

        // Bundle in our array values to the graph screen
        graphScreen.putExtra("SITE_NAME", passingValues[0]);
        graphScreen.putExtra("OPERATOR_NAME", passingValues[1]);
        graphScreen.putExtra("SAMPLE_ID", passingValues[2]);
        graphScreen.putExtra("TEMPERATURE", passingValues[3]);
        graphScreen.putExtra("COMMENTS", passingValues[4]);
        //graphScreen.putExtra("IMAGEPATH", mCurrentPhotoPath);
        graphScreen.putExtra("IMAGE", image);
        graphScreen.putExtra("TIME", currentDateTimeFormatted);
        graphScreen.putExtra("GPSLong", passingValues[5]);
        graphScreen.putExtra("GPSLat", passingValues[6]);
        graphScreen.putExtra("ELEVATION", passingValues[7]);

        // Start the graph screen
        startActivity(graphScreen);

    }

    public void goHomeScreen(View view)
    {
        String n  = OperatorName.getText().toString();
        String s  = SiteName.getText().toString();
        String a  = sampleID.getText().toString();

        String NULL = "";

        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString(K_Name, n);
        editor.putString(K_Site, s);
        editor.putString(K_ID, a);
        editor.putString(K_Temp, NULL);
        editor.putString(K_Comments, NULL);
        editor.putString(K_Long, NULL);
        editor.putString(K_Lat, NULL);
        editor.putString(K_Elev, NULL);
        editor.apply();

        Intent homeScreen;

        homeScreen = new Intent(this, homeScreen.class);

        startActivity(homeScreen);

    }

}
