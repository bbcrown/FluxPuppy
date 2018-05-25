package edu.nau.li_840a_interface;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.util.Base64;
import android.view.ViewGroup;


import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.w3c.dom.Text;

import edu.nau.li_840a_interface.R;

public class fileDirectory extends AppCompatActivity implements OnClickListener {

    EditText et_Filter;
    Button btnSend, btnView, btnDel, btnFilter, btnUndo, btnHome;
    private ListView lv;
    String message;
    Uri URI = null;
    private static final int PICK_FROM_GALLERY = 101;
    int viewFilePos;
    ArrayList<Uri> listOfUri = new ArrayList<Uri>();
    ArrayList<String> selectedFiles = new ArrayList<String>();
    ArrayList<String> tempFiles = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final Context context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_directory);

        // JIMMY ADDED CODE HERE
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
        //JIMMY IS DONE ADDING CODE HERE

        lv = (ListView) findViewById(R.id.listView);

        //SETS THE ARRAY LIST AS CLICKABLE
        lv.setChoiceMode(2);
        int i = 0;
        final String metaCheck = "M-";


       //Creates list that is integrated with ListView Object
        final List<String> appFiles = new ArrayList<String>();

        final ArrayList<String> sortedFiles = sortFiles(context.getFilesDir().list());
        //System.out.println("Sorted Files:" + sortedFiles);
        for (i = 0; i < sortedFiles.size() ; i++) {
            if (sortedFiles.get(i).contains(metaCheck)) {
                appFiles.add(sortedFiles.get(i).substring(2));
            }


        }


        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                appFiles){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                // Get the Item from ListView
                View view = super.getView(position, convertView, parent);

                // Initialize a TextView for ListView each Item
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)

                if(lv.isItemChecked(position) == true){
                    tv.setBackgroundColor(Color.BLUE);}
                else{tv.setBackgroundColor(Color.TRANSPARENT);}
                for(int p = 0; p < lv.getCount(); p++){
                    if(lv.isItemChecked(position) == true){ tv.setBackgroundColor(Color.GREEN);}
                    else{tv.setBackgroundColor(Color.TRANSPARENT);}
                }

                // Generate ListView Item using TextView
                return view;
            }
        };


        lv.setAdapter(arrayAdapter);

        //MAIN ITEM CLICKING FUNCTIONALITY
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                view.setSelected(true);
                File file = new File(context.getFilesDir(), "M-" + lv.getItemAtPosition(i).toString());
                File graphFile = new File(context.getFilesDir(), "G-" + lv.getItemAtPosition(i).toString());
                File imageFile = new File(context.getFilesDir(), "I-" + lv.getItemAtPosition(i).toString().substring(0, lv.getItemAtPosition(i).toString().length() - 4) + ".png");
                if(lv.getItemAtPosition(i).toString().contains("_subgraph_")) {
                    imageFile = new File(context.getFilesDir(), "I-" + lv.getItemAtPosition(i).toString().substring(0, lv.getItemAtPosition(i).toString().length() - 4).split("_subgraph_")[0] + ".png");
                }

                if (lv.isItemChecked(i) == false) {


                    Uri uri = FileProvider.getUriForFile(context, "edu.nau.li_840a_interface", file);
                    Uri graphUri = FileProvider.getUriForFile(context, "edu.nau.li_840a_interface", graphFile);
                    Uri imageUri = FileProvider.getUriForFile(context, "edu.nau.li_840a_interface", imageFile);

                    listOfUri.remove(uri);
                    listOfUri.remove(graphUri);
                    listOfUri.remove(imageUri);
                }
                if (lv.isItemChecked(i) == true) {

                    Uri uri = FileProvider.getUriForFile(context, "edu.nau.li_840a_interface", file);
                    Uri graphUri = FileProvider.getUriForFile(context, "edu.nau.li_840a_interface", graphFile);
                    Uri imageUri = FileProvider.getUriForFile(context, "edu.nau.li_840a_interface", imageFile);
                    listOfUri.add(uri);
                    listOfUri.add(graphUri);
                    listOfUri.add(imageUri);



                }
                for( int j = 0; j < lv.getCount(); j++)
                    if(lv.isItemChecked(j) == true ){

                        viewFilePos = j;}
                checkFileCount();


                arrayAdapter.getView(i, view, (ViewGroup)view.getParent());


                //DELETE FUNCTIONALITY, ALL HIGHLIGHTED FILES ARE DELETED WHEN BUTTON'S PRESSED
                btnDel.setOnClickListener(new View.OnClickListener() {
                    int j;
                    int listSize = lv.getCount();
                    @Override
                    public void onClick(View view) {
                        listOfUri.clear();
                        int count = 0;
                        //Creates warning popup when deleting files
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(context);
                        builder.setTitle("DELETING FILES")
                                .setMessage("Are you sure you want to delete these files?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        int count = 0;
                                        //DOUBLE LOOP THRU SELECTED FILES AND LVCOUNT
                                        for( j = 0; j < listSize; j++){
                                            if(lv.isItemChecked(j) == true ) {


                                                File file = new File(context.getFilesDir(), "M-" + lv.getItemAtPosition(j - count).toString());
                                                File graphFile = new File(context.getFilesDir(), "G-" + lv.getItemAtPosition(j - count).toString());

                                                File imageFile = new File(context.getFilesDir(), "I-" + lv.getItemAtPosition(j - count).toString().substring(0, lv.getItemAtPosition(j - count).toString().length() - 4) + ".png");




                                                if (j > 0) {


                                                    //ADD ITEM TO LIST FOR APPFILES TO REMOVE OUTSIDE OF LOOP


                                                    file.delete();
                                                    graphFile.delete();
                                                    imageFile.delete();
                                                    lv.setItemChecked((j), false);

                                                }

                                                else {
                                                    file.delete();
                                                    graphFile.delete();
                                                    imageFile.delete();

                                                }
                                                appFiles.remove(appFiles.indexOf(lv.getItemAtPosition(j - count)));
                                                lv.setItemChecked((j - count), false);
                                                count = count +1;

                                            }



                                            arrayAdapter.notifyDataSetChanged();}


                                        for(int l= 0; l < lv.getCount(); l++){
                                            lv.setItemChecked(l, false);

                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();


                        //No files will be selected so no buttons can be pressed
                        arrayAdapter.notifyDataSetChanged();
                        selectedFiles.clear();
                        checkFileCount();
                        viewFilePos = 0;
                        btnView.setClickable(false);
                        btnView.setBackgroundColor(Color.TRANSPARENT);
                        btnDel.setClickable(false);
                        btnDel.setBackgroundColor(Color.TRANSPARENT);
                        btnSend.setClickable(false);
                        btnSend.setBackgroundColor(Color.TRANSPARENT);
                        System.out.println("Files still selected :" + lv.getCheckedItemCount());
                    }





                });


            }


        });
        //SETS BUTTONS AND EDITEXT OBJECTS
        et_Filter = (EditText) findViewById(R.id.et_filter);
        btnUndo = (Button) findViewById(R.id.btn_undoFilter);
        btnSend = (Button) findViewById(R.id.btn_email);
        btnView = (Button) findViewById(R.id.btn_view);
        btnDel = (Button) findViewById(R.id.btn_delete);
        btnFilter =(Button) findViewById(R.id.btn_filter);
        btnHome = (Button) findViewById(R.id.button5);
        btnSend.setOnClickListener(this);
        btnView.setOnClickListener(this);
        btnView.setClickable(false);
        btnView.setBackgroundColor(Color.TRANSPARENT);
        btnUndo.setClickable(false);
        btnUndo.setBackgroundColor(Color.TRANSPARENT);
        btnSend.setClickable(false);
        btnSend.setBackgroundColor(Color.TRANSPARENT);




        //Filter functionality. User enters string into text field; when button is pressed
        //the list is restructured, containing only files with names containing the input
        btnFilter.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(et_Filter.getText().toString().equals("") ){return;}
                btnUndo.setClickable(true);
                btnUndo.setBackgroundResource(android.R.drawable.btn_default);
                int listSize = lv.getCount();
                for(int l= 0; l < lv.getCount(); l++){
                    lv.setItemChecked(l, false);

                }
                checkFileCount();
                String filter = et_Filter.getText().toString();
                for (int l = 0; l < lv.getCount(); l++){
                    if((lv.getItemAtPosition(l).toString().toLowerCase()).contains(filter.toLowerCase()) == true){
                        tempFiles.add(lv.getItemAtPosition(l).toString());


                    }
                }
                appFiles.clear();
                arrayAdapter.clear();
                for(int test =0; test < tempFiles.size(); test++)
                {
                    appFiles.add(tempFiles.get(test));
                }

                btnFilter.setBackgroundColor(Color.TRANSPARENT);
                btnFilter.setClickable(false);


            }
        });




        //Undoes the effect of the filter method, recreating directory list as normal
        btnUndo.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View view) {
                btnUndo.setClickable(false);
                btnUndo.setBackgroundColor(Color.TRANSPARENT);
                appFiles.clear();
                arrayAdapter.clear();
                tempFiles.clear();

                for (int i = 0; i < context.getFilesDir().listFiles().length - 1; i++) {
                    if (sortedFiles.get(i).contains(metaCheck)) {
                        appFiles.add(sortedFiles.get(i).substring(2));
                    }

                    else {
                        //appFiles.add(context.getFilesDir().list()[i]);
                    }


                }
                btnFilter.setBackgroundResource(android.R.drawable.btn_default);
                btnFilter.setClickable(true);

                for(int l= 0; l < lv.getCount(); l++){
                    lv.setItemChecked(l, false);

                }
                checkFileCount();

            }





        });
    }


    @Override
    public void onClick(View v) {

        //When a single file is selected for viewing, this method takes the names of the
        //files in the selected data set and transfers them into the view screen.
        if (v == btnView) {

            Button button = findViewById(R.id.btn_view);
            button.setText(R.string.load);

            Intent viewScreen;

            viewScreen = new Intent(this, viewScreen.class);
            viewScreen.putExtra("FILE", "M-" + lv.getItemAtPosition(viewFilePos).toString());
            viewScreen.putExtra("GRAPHFILE", "G-" + lv.getItemAtPosition(viewFilePos).toString());
            viewScreen.putExtra("IMAGE", "I-" + lv.getItemAtPosition(viewFilePos).toString().substring(0, lv.getItemAtPosition(viewFilePos).toString().length() - 4) + ".png");
            startActivity(viewScreen);

        }

        //All of the email functionality is written in here
        if (v == btnSend) {
            try {
                Context context = this;


                final Intent emailIntent = new Intent(

                        Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(emailIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    context.grantUriPermission(packageName, listOfUri.get(0), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_STREAM, listOfUri);
                if (URI != null) {
                    emailIntent.putExtra(Intent.EXTRA_STREAM, URI);
                }
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
                this.startActivity(Intent.createChooser(emailIntent,
                        "Sending email..."));

            } catch (Throwable t) {
                Toast.makeText(this,
                        "Request failed try again: " + t.toString(),
                        Toast.LENGTH_LONG).show();
            }
        }

    }

    //Method that handles making the buttons clickable or not, based on how many items are chosen
    public void checkFileCount() {
        if (lv.getCheckedItemCount() > 0 && lv.getCheckedItemCount() != 1) {
            btnView.setClickable(false);
            btnView.setBackgroundColor(Color.TRANSPARENT);
            btnSend.setBackgroundResource(android.R.drawable.btn_default);
            btnSend.setClickable(true);
            btnDel.setBackgroundResource(android.R.drawable.btn_default);
            btnDel.setClickable(true);



        } if(lv.getCheckedItemCount() == 1) {

            btnSend.setClickable(true);
            btnSend.setBackgroundResource(android.R.drawable.btn_default);
            btnView.setClickable(true);
            btnView.setBackgroundResource(android.R.drawable.btn_default);
            btnDel.setBackgroundResource(android.R.drawable.btn_default);
            btnDel.setClickable(true);
        }
        if(lv.getCheckedItemCount() == 0){
            btnSend.setBackgroundColor(Color.TRANSPARENT);
            btnSend.setClickable(false);
            btnView.setClickable(false);
            btnView.setBackgroundColor(Color.TRANSPARENT);
            btnDel.setBackgroundColor(Color.TRANSPARENT);
            btnDel.setClickable(false);
        }

    }

    //This is a simple method that is attached  to the home button that takes user to home screen
    public void goHomeScreen(View view)
    {

        Intent homeScreen;

        homeScreen = new Intent(this, homeScreen.class);

        startActivity(homeScreen);

    }

    //This method is responsible for sorting the files into a newest-first ordering
    ArrayList<String> sortFiles(String[] allFiles){

        ArrayList<String> tempArrayList = new ArrayList<String>();
        ArrayList<String> returnArrayList = new ArrayList<String>();
        String[] tempArray = allFiles;
        String[] returnArray;

        int numOfFiles = tempArray.length;

        //delete nonneeded files
        for(int k = 0; k < numOfFiles; k++){
            if (tempArray[k].startsWith("G-") || tempArray[k].startsWith("M-") || tempArray[k].startsWith("I-")){
                tempArrayList.add(tempArray[k]);
            }
        }

        returnArray = new String[tempArrayList.size()];
        for(int i = 0; i < tempArrayList.size(); i++){
            returnArray[i] = tempArrayList.get(i);
        }

        //if 0 return empty array
        if (returnArray.length < 2){
            return returnArrayList;
        }

        //bubble sort
        for (int i = 0; i < returnArray.length-1; i++){
            for (int j = 0; j < returnArray.length-i-1; j++){
                //get string of full number from file
                String tempFirst = returnArray[j].split("_")[2] + returnArray[j].split("_")[3];
                String tempSecond = returnArray[j+1].split("_")[2] + returnArray[j+1].split("_")[3];
                //take out csv if it is not a subgraph
                if (!tempFirst.contains("_subgraph_")){
                    tempFirst = tempFirst.substring(0,14);
                }
                if (!tempSecond.contains("_subgraph_")){
                    tempSecond = tempSecond.substring(0,14);
                }
                //get int of string
                long tempFirstNum = Long.parseLong(tempFirst);
                long tempSecondNum = Long.parseLong(tempSecond);
                //compare and swap if bigger
                if (tempFirstNum >= tempSecondNum)
                {
                    String temp = returnArray[j];
                    returnArray[j] = returnArray[j+1];
                    returnArray[j+1] = temp;
                }
            }
        }

        //add elements to arraylist with newest date being first
        for(int k = returnArray.length-1; k >= 0; k--){
            returnArrayList.add(returnArray[k]);
            System.out.println(allFiles[k]);
        }

        return returnArrayList;
    }

}
