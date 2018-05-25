package edu.nau.li_840a_interface;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.String;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Double.NaN;


public class viewScreen extends AppCompatActivity {

    String metaData;
    String[] metaDataArray;
    String metaFile;
    String graphFile;
    String graphData;
    String imageFile;
    String imageData;
    Double regressionSlope;
    Double yIntercept;
    boolean changedValue = false;

    //graph variables
    private GraphView graphIds[];
    private TextView textIds[];
    private String graphArray[];

    private String newGraph;
    private String newMeta;
    private String newImage;

    private LineGraph co2Graph;
    private LineGraph h2oGraph;
    private LineGraph tempGraph;
    private LineGraph presGraph;

    private TextView slope;
    private TextView standardError;
    private TextView rSquared;
    public EditText comments;
    private String origionalComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        metaDataArray = new String[9];

        //set up UI
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_screen_test);

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

        TextView site = findViewById(R.id.tv_SiteName);
        TextView operator = findViewById(R.id.tv_OpName);
        TextView sampleID = findViewById(R.id.tv_SampleID);
        TextView temperature = findViewById(R.id.tv_Temp);
        comments = findViewById(R.id.et_comments);
        TextView timeAndDate = findViewById(R.id.tv_TimeDate);
        TextView fileName = findViewById(R.id.tv_FileName);
        TextView longitude = findViewById(R.id.tv_long);
        TextView latitude = findViewById(R.id.tv_lat);
        TextView elevation = findViewById(R.id.tv_elev);



        slope = findViewById(R.id.slope);
        standardError = findViewById(R.id.standarderror);
        rSquared = findViewById(R.id.rsquared);



        //graph ids array
        graphIds = new GraphView[4];
        graphIds[0] = findViewById(R.id.graph1);
        graphIds[1] = findViewById(R.id.graph2);
        graphIds[2] = findViewById(R.id.graph3);
        graphIds[3] = findViewById(R.id.graph4);

        //graph text views
        textIds = new TextView[4];
        textIds[0] = findViewById(R.id.co2display);
        textIds[1] = findViewById(R.id.h2odisplay);
        textIds[2] = findViewById(R.id.tempdisplay);
        textIds[3] = findViewById(R.id.presdisplay);

        // Image
        ImageView metaImageDisplay = findViewById(R.id.metaDataImage);

        //try to open file and save string metadata
        try {

            metaFile = getIntent().getStringExtra("FILE");
            InputStream inputStream = openFileInput(metaFile);
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader buffReader = new BufferedReader(inputReader);
            metaData = "";
            String line = buffReader.readLine();
            while(line != null)
            {
                metaData += line + "\n";
                line = buffReader.readLine();
            }

            graphFile = getIntent().getStringExtra("GRAPHFILE");
            inputStream = openFileInput(graphFile);
            inputReader = new InputStreamReader(inputStream);
            buffReader = new BufferedReader(inputReader);
            graphData = "";
            line = buffReader.readLine();
            while(line != null)
            {
                graphData += line + "\n";
                line = buffReader.readLine();

            }

            if (!metaFile.contains("_subgraph_")) {
                imageFile = getIntent().getStringExtra("IMAGE");
                inputStream = openFileInput(imageFile);
                inputReader = new InputStreamReader(inputStream);
                buffReader = new BufferedReader(inputReader);
                imageData = "";
                line = buffReader.readLine();
                while (line != null) {
                    imageData += line;
                    line = buffReader.readLine();
                }
            }
            else{
                imageFile = getIntent().getStringExtra("IMAGE");
                imageFile = imageFile.split("_subgraph_")[0] + ".png";
            }

        //return error and null for all data if it cannot open
        } catch (Exception e) {

            fileName.setText("Sorry there was an error opening your file, please try again!");
            fileName.setText(null);
            operator.setText(null);
            site.setText(null);
            sampleID.setText(null);
            temperature.setText(null);
            comments.setText(null);
            timeAndDate.setText(null);
            longitude.setText(null);
            latitude.setText(null);
            elevation.setText(null);


        }
        //if subgraph dont reread just put in
        try
        {
            FileInputStream stream = openFileInput(imageFile);
            metaImageDisplay.setImageBitmap(BitmapFactory.decodeStream(stream));
        }
        catch(Exception exception)
        {

        }


        metaDataArray = metaData.split("\n")[1].split(",");


        // Put text in each box
        fileName.setText("FILE : " + metaFile.substring(2));
        operator.setText("Operator Name : " + metaDataArray[0]);
        site.setText("Site  Name : " + metaDataArray[1]);
        sampleID.setText("Sample ID : " + metaDataArray[2]);
        temperature.setText("Temperature : " + metaDataArray[3]);
        comments.setText(metaDataArray[4]);
        timeAndDate.setText("Time and Date : " + metaDataArray[5]);
        longitude.setText("Longitude : " + metaDataArray[6]);
        latitude.setText("Latitude : " + metaDataArray[7]);
        elevation.setText("Elevation : " + metaDataArray[8]);

        // set origional comments for checking if they changed later
        origionalComments = comments.getText().toString();

        //get graph info to string array
        graphArray = new String[4];

        try
        {

            graphArray = splitGraphData(graphData);

            //format decimal to be three points
            DecimalFormat df = new DecimalFormat("#0.0000");

            slope.setText("Regression Slope : " + df.format(getRegressionSlope(graphArray[0])));
            standardError.setText("Standard Error : " + df.format(getStandardError(graphArray[0])));
            rSquared.setText("R Squared : " + df.format(getRSquared(graphArray[0])));

        }


        catch(Exception exception)
        {
            graphArray = new String[4];
            graphArray[0] = "";
            graphArray[1] = "";
            graphArray[2] = "";
            graphArray[3] = "";
        }


        // get regression and y intercept of co2
        regressionSlope = getRegressionSlope(graphArray[0]);
        yIntercept = getYIntercept(graphArray[0]);

        // Set up graphs with data
        co2Graph = new LineGraph(graphIds[0], "CO2", "Time", "CO2",
                Color.argb(255, 0, 0, 0), graphArray[0]);
        h2oGraph = new LineGraph(graphIds[1], "H2O", "Time", "H2O",
                Color.argb(255, 0, 0, 255), graphArray[1]);
        tempGraph = new LineGraph(graphIds[2], "Temperature", "Time", "Temperature",
                Color.argb(255, 255, 0, 0), graphArray[2]);
        presGraph = new LineGraph(graphIds[3], "Pressure", "Time", "Pressure",
                Color.argb(255, 0, 125, 0), graphArray[3]);

        //add a regression line to the co2 graph
        co2Graph.resetZoom();
        co2Graph.addRegLine(yIntercept, regressionSlope);

    }

    public void showCO2(View view)
    {

        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        co2Graph.resetZoom();


    }

    public void showH2O(View view)
    {

        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        h2oGraph.resetZoom();
    }

    public void showTemp(View view)
    {

        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        tempGraph.resetZoom();
    }

    public void showPres(View view)
    {

        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(0, 0));

        presGraph.resetZoom();
    }

    private String[] splitGraphData(String graphFileContents)
    {

        String lines[];
        String values[];
        String output[];
        String co2Points;
        String h2oPoints;
        String tempPoints;
        String presPoints;
        int count;

        co2Points = "";
        h2oPoints = "";
        tempPoints = "";
        presPoints = "";

        lines = graphFileContents.split("\n");

        for (count = 1; count < lines.length; count++)
        {

            values = lines[count].split(",");

            co2Points += values[0] + "," + values[1] + "\n";
            h2oPoints += values[0] + "," + values[2] + "\n";
            tempPoints += values[0] + "," + values[3] + "\n";
            presPoints += values[0] + "," + values[4] + "\n";

        }

        output = new String[4];

        output[0] = co2Points;
        output[1] = h2oPoints;
        output[2] = tempPoints;
        output[3] = presPoints;

        return output;

    }

    private double getStandardError(String graphPoints){

        String[] data;
        String[] tempData;
        int numOfPoints;

        if (graphPoints == ""){
            return 0;
        }

        SimpleRegression SR = new SimpleRegression();


        //split data
        data = graphPoints.split("\n");
        numOfPoints = data.length;

        //Dr Richardson wanted this if there are not at least 5 points
        if (numOfPoints < 5) {
            return NaN;
        }

        //loop through and get mean of all points
        for(int i = 0; i < numOfPoints; i++){
            tempData = data[i].split(",");
            SR.addData( Double.parseDouble(tempData[0]),  Double.parseDouble(tempData[1]));
        }

        return SR.getSlopeStdErr();


    }

    private double getRegressionSlope(String graphPoints){

        String[] data;
        String[] tempData;
        int numOfPoints;

        if (graphPoints == ""){
            return 0;
        }

        SimpleRegression SR = new SimpleRegression();


        //split data
        data = graphPoints.split("\n");
        numOfPoints = data.length;

        //loop through and get mean of all points
        for(int i = 0; i < numOfPoints; i++){
            tempData = data[i].split(",");
            SR.addData( Double.parseDouble(tempData[0]),  Double.parseDouble(tempData[1]));
        }

        return SR.getSlope();


    }

    private double getYIntercept(String graphPoints){

        String[] data;
        String[] tempData;
        int numOfPoints;

        if (graphPoints == ""){
            return 0;
        }

        SimpleRegression SR = new SimpleRegression();


        //split data
        data = graphPoints.split("\n");
        numOfPoints = data.length;


        //loop through and get mean of all points
        for(int i = 0; i < numOfPoints; i++){
            tempData = data[i].split(",");
            SR.addData( Double.parseDouble(tempData[0]),  Double.parseDouble(tempData[1]));
        }

        return SR.getIntercept();


    }

    private void setXRange(String graphPoints){

        String[] tempData;
        String[] firstData;
        String[] lastData;
        String[] reusedValues;
        int counter;
        double firstSecond;
        double lastSecond;

        //split data
        tempData = graphPoints.split("\n");

        firstData = tempData[0].split(",");
        lastData = tempData[tempData.length - 1].split(",");

        firstSecond = Float.parseFloat(firstData[0]);
        lastSecond = Float.parseFloat(lastData[0]);

        //change csv
        // Get the new meta
        newMeta = metaData.split("\n")[0] + "\n";
        reusedValues = metaData.split("\n")[1].split(",");

        for (counter = 0; counter <= 11; counter++)
        {
            newMeta += reusedValues[counter] + ",";
        }

        DecimalFormat df = new DecimalFormat("#0.0000");


        newMeta += df.format(firstSecond) + ",";
        newMeta += df.format(lastSecond);
    }

    private double getRSquared(String graphPoints){

        double xTotal = 0;
        double yTotal = 0;
        double xSquaredTotal = 0;
        double ySquaredTotal = 0;
        double XY = 0;
        double rSquared = 0;
        String[] data;
        String[] tempData;
        int numOfPoints;
        double tempX;
        double tempY;

        if (graphPoints == ""){
            return 0.0;
        }

        //split data
        data = graphPoints.split("\n");
        numOfPoints = data.length;

        //Dr Richardson wanted this if there are not at least 5 points
        if (numOfPoints < 5) {
            return NaN;
        }

        //loop through and get xtotal and ytotal and their squares
        for(int i = 0; i < numOfPoints; i++){
            tempData = data[i].split(",");

            tempX = Double.parseDouble(tempData[0]);
            tempY = Double.parseDouble(tempData[1]);

            //get x and y
            xTotal += tempX;
            yTotal += tempY;

            //get x and y rSquared
            xSquaredTotal += (tempX * tempX);
            ySquaredTotal += (tempY * tempY);

            //get sum of x*y
            XY += (tempX * tempY);
        }

        //rsquared equation
        rSquared = (((numOfPoints * XY) - (xTotal * yTotal)) /
                ((Math.sqrt((numOfPoints * xSquaredTotal) - (xTotal*xTotal))) * (Math.sqrt((numOfPoints * ySquaredTotal) - (yTotal*yTotal)))));

        //square r
        rSquared  = (rSquared * rSquared);

        return rSquared;
    }

    /*
     *
     */
    public void goToFileDirectory(View view)
    {
        final View passView = view;
        FileOutputStream outStream;
        String newComments;

        // check if there was an update
        updateOrigMetaComments();

        newComments = metaData.split("\n")[1].split(",")[4];

        //if comments are changed and they exit
        if (!newComments.equals(origionalComments)) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Save new comments?");
            builder.setMessage("Are you sure you want to save new comments to this file?");
            builder.setIcon(android.R.drawable.ic_dialog_alert);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    goToFD(passView);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });

            builder.show();


            try {
                outStream = openFileOutput(metaFile, Context.MODE_PRIVATE);
                outStream.write(metaData.getBytes());
                outStream.close();
            } catch (Exception e) {

            }
            //if subgraph applied and they try to exit
        } else if( changedValue == true) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Do not save subgraph?");
            builder.setMessage("Are you sure you want to exit without saving a subgraph?");
            builder.setIcon(android.R.drawable.ic_dialog_alert);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    goToFD(passView);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    return;
                }
            });

            builder.show();
            //else go to FD
        } else {
            goToFD(passView);
        }
    }

    public void goToFD(View view){

        Intent fileDirectory;

        fileDirectory = new Intent(viewScreen.this, fileDirectory.class);

        startActivity(fileDirectory);
    }

    public void saveAndGoToFileDirectory(View view)
    {
        final View passView = view;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Save new data subset?");
        builder.setMessage("Are you sure you want to save a new data subset? This will not " +
                "overwrite any previously recorded data sets.");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveAndGo(passView);
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.show();
    }

    /*
     *
     */
    public void saveAndGo(View view)
    {

        Intent fileDirectory;
        Date currentDate;
        DateFormat dateAndTimeFormat;
        String currentDateTimeFormatted;
        String oldFileName;
        String newFileName;
        String newGraphFileName;
        String newMetaFileName;
        FileOutputStream outStream;


        currentDate = new Date();

        // Format time to filename
        dateAndTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        currentDateTimeFormatted = dateAndTimeFormat.format(currentDate);

        oldFileName = getIntent().getStringExtra("GRAPHFILE");
        oldFileName = oldFileName.substring(2, oldFileName.indexOf("."));

        if (oldFileName.contains("_subgraph_"))
        {
            newFileName = oldFileName.split("_subgraph_")[0] + "_subgraph_" + currentDateTimeFormatted + ".csv";
        }
        else
        {
            newFileName = oldFileName + "_subgraph_" + currentDateTimeFormatted + ".csv";
        }

        newGraphFileName = "G-" + newFileName;
        newMetaFileName = "M-" + newFileName;

        updateSubMetaComments();
        updateOrigMetaComments();

        // Write the new subsection to the graph file
        try
        {
            // New graph file
            outStream = openFileOutput(newGraphFileName, Context.MODE_PRIVATE);
            outStream.write(newGraph.getBytes());
            outStream.close();

            // New meta file
            outStream = openFileOutput(newMetaFileName, Context.MODE_PRIVATE);
            outStream.write(newMeta.getBytes());
            outStream.close();
            //TODO: check if we need to rewrite comments
            //rewrite origional comments
            outStream = openFileOutput(metaFile, Context.MODE_PRIVATE);
            outStream.write(metaData.getBytes());
            outStream.close();
        }
        catch(Exception exception)
        {

        }


        fileDirectory = new Intent(this, fileDirectory.class);

        startActivity(fileDirectory);

    }

    public void updateSubMetaComments(){

        String[] reusedValues;
        int counter;
        String newComments;

        newComments = comments.getText().toString();

        newMeta = metaData.split("\n")[0] + "\n";
        reusedValues = metaData.split("\n")[1].split(",");


        // reuse origional
        for (counter = 0; counter <= 3; counter++)
        {
            newMeta += reusedValues[counter] + ",";
        }
        // change comments
        newMeta += newComments + ",";

        //reuse the rest of the values
        for (counter = 5; counter <= 12; counter++)
        {
            newMeta += reusedValues[counter] + ",";
        }
        //add last value without a comma
        newMeta += reusedValues[counter];
        return;
    }

    public void updateOrigMetaComments(){

        String[] reusedValues;
        int counter;
        String newComments;
        String tempMeta;

        newComments = comments.getText().toString();

        tempMeta = metaData.split("\n")[0] + "\n";
        reusedValues = metaData.split("\n")[1].split(",");


        // reuse origional
        for (counter = 0; counter <= 3; counter++)
        {
            tempMeta += reusedValues[counter] + ",";
        }
        // change comments
        tempMeta += newComments + ",";

        //reuse the rest of the values
        for (counter = 5; counter <= 12; counter++)
        {
            tempMeta += reusedValues[counter] + ",";
        }
        //add last value without a comma
        tempMeta += reusedValues[counter];
        metaData = tempMeta;
        return;
    }

    public void applySubgraph(View view)
    {

        EditText splitText;
        String splitTextContent;
        Button saveExitButton = findViewById(R.id.saveAndFileDirectoryButton);
        int counter;
        String[] reusedValues;
        String newSlope;
        String newRSquared;
        String newStdError;
        String newRegSlope;

        //Get the string value from the text box
        splitText = findViewById(R.id.splitText);
        splitTextContent = splitText.getText().toString();

        // If the string entered into the text box is not valid, alert the user and do nothing else
        if (!validEntry(splitTextContent, graphData)) {
            return;
        }

        // If the string entered into the text bos is valid...
        else
        {

            // Chop the graph file string down to the the new subsection
            newGraph = chopString(graphData, splitTextContent.split(",")[0], splitTextContent.split(",")[1]);

            // Initialize an array to hold the data for each specific graph
            graphArray = new String[4];

            // TODO: This might not be a necessary try-catch. Experiment with removing it
            try
            {

                // Split the new graph data into four strings
                graphArray = splitGraphData(newGraph);

                // Format decimal to be three points
                DecimalFormat df = new DecimalFormat("#0.0000");

                newSlope = df.format(getRegressionSlope(graphArray[0]));
                newStdError = df.format(getStandardError(graphArray[0]));
                newRSquared = df.format(getRSquared(graphArray[0]));
                newRegSlope = df.format(getRegressionSlope(graphArray[0]));

                // Assign the new stats to the UI text boxes
                slope.setText("Regression Slope : " + newSlope);
                standardError.setText("Standard Error : " + newStdError);
                rSquared.setText("R Squared : " + newRSquared);

            }
            catch(Exception exception)
            {
                graphArray = new String[4];
                graphArray[0] = "";
                graphArray[1] = "";
                graphArray[2] = "";
                graphArray[3] = "";
                newStdError = "";
                newRSquared = "";
                newRegSlope = "";
            }

            // Get the new meta
            newMeta = metaData.split("\n")[0] + "\n";
            reusedValues = metaData.split("\n")[1].split(",");

            for (counter = 0; counter <= 8; counter++)
            {
                newMeta += reusedValues[counter] + ",";
            }

            newMeta += newRSquared + ",";
            newMeta += newRegSlope + ",";
            newMeta += newStdError;

            // Get the new image
            newImage = imageData;

            yIntercept = getYIntercept(graphArray[0]);

            // Set up graphs with data
            co2Graph = new LineGraph(graphIds[0], "CO2", "Time", "CO2",
                    Color.argb(255, 0, 0, 0), graphArray[0]);
            h2oGraph = new LineGraph(graphIds[1], "H2O", "Time", "H2O",
                    Color.argb(255, 0, 0, 255), graphArray[1]);
            tempGraph = new LineGraph(graphIds[2], "Temperature", "Time", "Temperature",
                    Color.argb(255, 255, 0, 0), graphArray[2]);
            presGraph = new LineGraph(graphIds[3], "Pressure", "Time", "Pressure",
                    Color.argb(255, 0, 125, 0), graphArray[3]);

            //add a regression line to the co2 graph
            co2Graph.addRegLine(yIntercept, Float.parseFloat(newRegSlope));

            saveExitButton.setBackgroundResource(android.R.drawable.btn_default);
            saveExitButton.setEnabled(true);
            setXRange(graphArray[0]);

        }

    }

    /*
     * param - graph as a string, starting point, ending point
     * return - new chopped string
     * function to cut a string based off a start and end point
     */
    static String chopString(String graphPoints, String xStart, String xEnd){

        String[] tempData;
        String newString = "";
        int indexOfStart = 1;
        int indexOfEnd = 1;


        //split data
        tempData = graphPoints.split("\n");
        float tempSecond = Float.parseFloat(tempData[1].split(",")[0]);

        //loop through points until start is closest to graph starting
        while ( tempSecond < Float.parseFloat(xStart)){
            tempSecond = Float.parseFloat(tempData[indexOfStart++].split(",")[0]);
        }

        if (indexOfStart != 1){
            indexOfStart -= 1;
        }

        //set the end to start chekcing where start left off
        indexOfEnd = indexOfStart;

        //loop through points until end is closest to ending
        while ( tempSecond < Float.parseFloat(xEnd)){
            tempSecond = Float.parseFloat(tempData[indexOfEnd++].split(",")[0]);

        }



        indexOfEnd -= 1;

        //add headers
        newString += tempData[0] + "\n";


        //loop through array and create string
        for(int i = indexOfStart; i < indexOfEnd; i++ ){
            newString += tempData[i] + "\n";
        }

        return newString;
    }

    /*
     * param - csv start and end for graph ie x,y
     * return - true if valid entry false otherwise
     * function to check if xstart and xend is a valid entry
     */
    boolean validEntry(String xStartAndEnd, String graphString){

        double firstSecond = 0.0;
        double lastSecond = 0.0;
        double xStartInt = 0.0;
        double xEndInt = 0.0;
        String xStart = "";
        String xEnd = "";
        String[] firstData;
        String[] lastData;
        String[] tempData;

        if (graphString == ""){
            return false;
        }

        //split graph data
        tempData = graphString.split("\n");

        //get first and last points in array of graph
        firstData = tempData[1].split(",");
        lastData = tempData[tempData.length - 1].split(",");

        //get first and last second of graph
        firstSecond = Double.parseDouble(firstData[0]);
        lastSecond = Double.parseDouble(lastData[0]);

        // if null or not csv
        if (xStartAndEnd == ""){
            return false;
        }
        if (!xStartAndEnd.contains(",")){
            Toast.makeText(viewScreen.this,"Not a valid entry: must contain a comma",Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            //get strings of ints
            xStart = xStartAndEnd.split(",")[0];
            xEnd = xStartAndEnd.split(",")[1];
        } catch (Exception e) {
            Toast.makeText(viewScreen.this,"Not a valid entry: enter two numbers",Toast.LENGTH_LONG).show();
            return false;
        }

        //added for number, error
        if (xStart == "" || xEnd == ""){
            return false;
        }

        //if not all numbers
        if (!xStart.matches("[0-9]+") || !xEnd.matches("[0-9]+")){
            Toast.makeText(viewScreen.this,"Not a valid entry: can only contain numbers",Toast.LENGTH_LONG).show();
            return false;
        }

        //get ints of strings
        xStartInt = Float.parseFloat(xStart);
        xEndInt = Float.parseFloat(xEnd);

        //if out of range or equal
        if (xStartInt < 0  || xStartInt > xEndInt || xStartInt == xEndInt){
            Toast.makeText(viewScreen.this,"Not a valid entry: must be in range " + firstSecond + " to " + lastSecond,Toast.LENGTH_LONG).show();
            return false;
        }

        //if out of graph string range
        if (xStartInt < firstSecond || xEndInt > lastSecond){
            Toast.makeText(viewScreen.this,"Not a valid entry: must be in range" + firstSecond + " to " + lastSecond,Toast.LENGTH_LONG).show();
            return false;
        }

        changedValue = true;
        return true;
    }


    @Override
    public void onBackPressed()
    {
        return;
    }

}
