/*
 *  Author: James Beasley
 *  Last updated: April 6th, 2018
 *  Description: Handles the initialization and updating of the graphs, keeping a log of all every
 *               data point, and converting that log into a string to it can be written to a file
 *               in the graph screen Java file. Implements the Runnable interface so that the graph
 *               updating can be done on its own thread, and not have to share with the main
 *               application thread.
 */
package edu.nau.li_840a_interface;

import android.app.Activity;
import android.graphics.Color;
import android.widget.TextView;
import com.jjoe64.graphview.GraphView;
import java.util.ArrayList;
import java.util.Date;

public class GraphManager implements Runnable
{

    ////////////////////////////
    // CLASS MEMBER VARIABLES //
    ////////////////////////////
    private ArrayList<DataSeries> dataArray;
    private Activity activity;
    private LineGraph co2Graph;
    private LineGraph h2oGraph;
    private LineGraph tempGraph;
    private LineGraph presGraph;
    private TextView co2Display;
    private TextView h2oDisplay;
    private TextView tempDisplay;
    private TextView presDisplay;
    private long startTime;
    private boolean running;
    private boolean logging;
    private String lastData;

    ///////////////
    // CONSTANTS //
    ///////////////
    private static final int SLEEP_TIME = 1000;


    /*
     *  Constructor for the GraphManager. Finds the start time, initializes the graphs, and sets
     *  other class member variables.
     */
    public GraphManager(Activity activity, GraphView[] graphIds, TextView textIds[])
    {

        Date time;

        // The activity is required for updating items on the UI thread, so we save it here as a
        // class member variable
        this.activity = activity;

        // Initialize our array of readings
        dataArray = new ArrayList<DataSeries>();

        // Initialize all of the graphs
        co2Graph = new LineGraph(graphIds[0], "CO2", "Time", "CO2",
                Color.argb(255, 0, 0, 0));
        h2oGraph = new LineGraph(graphIds[1], "H2O", "Time", "H2O",
                Color.argb(255, 0, 0, 255));
        tempGraph = new LineGraph(graphIds[2], "Temperature", "Time", "Temperature",
                Color.argb(255, 255, 0, 0));
        presGraph = new LineGraph(graphIds[3], "Pressure", "Time", "Pressure",
                Color.argb(255, 0, 125, 0));

        // Get the IDs for the text views used to display the data values
        co2Display = textIds[0];
        h2oDisplay = textIds[1];
        tempDisplay = textIds[2];
        presDisplay = textIds[3];

        // Get the start time
        time = new Date();
        startTime = time.getTime();

        // Set the graph in motion
        running = true;

        // Assume we are not logging a subset at the start
        logging = false;

    }

    /*
     *  Implementation of the runnable interface. Reads in data from the instrument, calculates the
     *  time that data came in, and adds the data to the graphs.
     */
    public void run()
    {

        String data;
        Date time;
        long currentTime;
        long timeDiff;

        // Loops until the screen is deconstructed
        while (running)
        {

            // Get the latest data from the instrument
            data = this.getData();

            // Get the current time
            time = new Date();
            currentTime = time.getTime();

            // Calculate the time difference between when the screen started
            timeDiff = currentTime - startTime;

            // Add the points to the graphs
            this.addPoints(data, timeDiff);

            // Wait the specified wait time
            try
            {
                Thread.sleep(SLEEP_TIME);
            }
            catch (Exception exception)
            {

            }

        }

    }

    /*
     *  This method is used by the SerialReader object to update the most recent full line from the
     *  gas analyzer.
     */
    public void updateData(String data)
    {
        lastData = data;
    }

    ////////////////////
    // BUTTON METHODS //
    ////////////////////

    /*
     *  Runs when the "Finalize" button is clicked. Tells the graph update loop to stop.
     */
    public void deconstruct()
    {

        // This variable is used to signal the while loop to stop
        running = false;

    }

    /*
     *  Runs when the "Start Log" button is clicked. Initializes the data log, and toggles the
     *  variable which specifies if we should be saving points.
     */
    public void startlogging()
    {

        //Date time;

        // Clear the log of any previously saved data points
        dataArray = new ArrayList<DataSeries>();


        // Signal that we should be adding points to the log
        logging = true;

        // Reset the time
        //time = new Date();
       // startTime = time.getTime();

        //resetGraphs();

    }

    /*
     *  Runs when the "Stop Log" button is clicked. Toggles the variable which specifies if we
     *  should be saving points.
     */
    public void stoplogging()
    {

        // Signal that we shouldn't be adding points to the log
        logging = false;
    }

    /*
     *  When the "Start Logging" button is pressed, the graph should be reset. This method calls
     *  each graphs individual reset method.
     */
    public void resetGraphs()
    {
        co2Graph.reset();
        h2oGraph.reset();
        tempGraph.reset();
        presGraph.reset();
    }

    /*
     *  When the "Enable Zoom" button is pressed, each graph's individual enableZoom method should
     *  be called.
     */
    public void enableZoom()
    {
        co2Graph.enableZoom();
        h2oGraph.enableZoom();
        tempGraph.enableZoom();
        presGraph.enableZoom();
    }

    /*
     *  When the "Disable Zoom" button is pressed, each graph's individual disableZoom method should
     *  be called.
     */
    public void disableZoom()
    {
        co2Graph.disableZoom();
        h2oGraph.disableZoom();
        tempGraph.disableZoom();
        presGraph.disableZoom();
    }

    /*
     *  Not linked to any button, but is used to test if the "Finalize" button should be enabled. If
     *  the graph manager is "empty", meaning if it has recorded no data, then the finalize button
     *  should not be clickable.
     */
    public boolean isEmpty()
    {
        if (dataArray.isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //////////////////////
    // FILE I/O METHODS //
    //////////////////////

    /*
     *  Allows the graph manager to be printed. This is used for storing the data of all the graphs
     *  in a CSV file.
     */
    public String toString()
    {

        String output;

        // Initialize the output string
        output = "";

        // Add the CSV header
        output += "Seconds,CO2,H2O,Temperature,Pressure\n";

        // Loop through each data series in the data array
        for (DataSeries series : dataArray)
        {

            // Append the series string and add a line break
            output += series.toString() + "\n";

        }

        // Return the final output string
        return output;

    }

    /////////////////////
    // PRIVATE METHODS //
    /////////////////////

    /*
     *  Simple getter function. Returns the last full line received from the serial reader.
     */
    private String getData()
    {
        return lastData;
    }

    /*
     *  Takes in a string of data from the instrument, converts it into a
     *  DataSeries, adds that DataSeries to the log, and updates each
     *  graph using the new information.
     */
    private void addPoints(String data, long time)
    {

        final DataSeries newSeries;

        // Initialize the new data series
        newSeries = new DataSeries(data, time);

        // If the data point is invalid, do not add it to the data array or graphs
        if (newSeries.co2 == 0 || newSeries.h2o == 0 || newSeries.temp == 0 || newSeries.pres == 0)
        {
            return;
        }

        // Add the new data series to the array of all data series
        if (logging)
        {
            dataArray.add(newSeries);
        }

        // These updates must be run on the UI thread in order to work
        activity.runOnUiThread(new Runnable() {
            public void run() {

                // Android GraphView currently has a bug which can cause
                // "ConcurrentModificationException"s when adding points to a graph series. Having
                // the manager add the new points on the UI thread is done to circumvent this bug.
                co2Graph.addPoint(newSeries.co2, newSeries.time);
                h2oGraph.addPoint(newSeries.h2o, newSeries.time);
                tempGraph.addPoint(newSeries.temp, newSeries.time);
                presGraph.addPoint(newSeries.pres, newSeries.time);

                // Update each text view with the data series
                co2Display.setText(String.valueOf(newSeries.co2));
                h2oDisplay.setText(String.valueOf(newSeries.h2o));
                tempDisplay.setText(String.valueOf(newSeries.temp));
                presDisplay.setText(String.valueOf(newSeries.pres));

            }
        });

    }

    /////////////////////
    // PRIVATE CLASSES //
    /////////////////////

    /*
     *  Basic object used to store and access values parsed from the data.
     */
    private class DataSeries
    {

        //public long time;
        public float time;
        public float co2;
        public float h2o;
        public float temp;
        public float pres;

        /*
         *  Constructor for the DataSeries. Takes in the data from the
         *  instrument, parses out the four relevant values, and assigns them
         *  to their corresponding variables. Also assigns the time of the data
         *  points based off the time parameter.
         */
        public DataSeries(String data, long time)
        {

            float[] parse;

            // Save the time the data series was initialized at
            this.time = (float) time / 1000;

            // Get an array of all the parsed values from the data
            parse = parseData(data);

            // Assign each value of the array to the class member variable
            co2 = parse[0];
            h2o = parse[1];
            temp = parse[2];
            pres = parse[3];

        }

        /*
         *  Takes in a string, and parses the data into an array of four float values. One for each
         *  of the graphs. If any problems are encountered while parsing out a particular value,
         *  then that value is set to zero. Assuming that the instrument is properly communicating
         *  with the application, then these exceptions should never occur, but under rare sets of
         *  circumstances, they might,
         */
        private float[] parseData(String data)
        {

            float[] output;

            // Initialize our array which will hold all the parsed values
            output = new float[4];

            // Try to parse out the CO2
            try
            {
                output[0] = stringToFloat(data.split("<co2>")[1].split("</co2>")[0]);
            }
            catch(Exception exception)
            {
                output[0] = (float) 0.0;
            }

            // Try to parse out the H2O
            try
            {
                output[1] = Float.parseFloat(data.split("<h2o>")[1].split("</h2o>")[0]);
            }
            catch(Exception exception)
            {
                output[1] = (float) 0.0;
            }

            // Try to parse out the temperature
            try
            {
                output[2] = stringToFloat(data.split("<celltemp>")[1].split("</celltemp>")[0]);
            }
            catch(Exception exception)
            {
                output[2] = (float) 0.0;
            }

            // Try to parse out the pressure
            try
            {
                output[3] = stringToFloat(data.split("<cellpres>")[1].split("</cellpres>")[0]);
            }
            catch(Exception exception)
            {
                output[3] = (float) 0.0;
            }

            // Return an array of all the parsed values
            return output;

        }

        /*
         *  In the data lines received from the gas analyzer, numbers are represented using a form
         *  of scientific notation. This function takes in a string of that and converts it to a
         *  usable, numerical float.
         */
        private float stringToFloat(String input)
        {

            int count;
            float number;
            int exponent;

            // Parse out the number, and the exponent
            number = Float.parseFloat(input.split("e")[0]);
            exponent = Integer.parseInt(input.split("e")[1]);

            // Multiply the number by the exponent
            for (count = 0; count < exponent; count++)
            {
                number *= 10;
            }

            // Return our final, calculated value
            return number;

        }

        /*
         *  Allows the series to be printed as string. Used for logging to a
         *  CSV file.
         */
        public String toString()
        {

            String output;

            // Initialize our output string
            output = "";

            // Add each value to the output string
            output += time + "," + co2 + "," + h2o + "," + temp + "," + pres;

            // Return the output
            return output;

        }

    }

}
