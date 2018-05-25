/*
 *  Author: James Beasley
 *  Last updated: April 6th, 2018
 *  Description: Uses the Android GraphView library to create a line graph. The graph is used on two
 *               different screens: the graph screen and the view screen. Graphs on the graph screen
 *               are initialized using the first constructor, which allows for data points to be
 *               added dynamically. Graphs on the view screen are initialized using the second
 *               constructor, and have their points specified by a string parameter.
 */
package edu.nau.li_840a_interface;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

public class LineGraph
{

    ////////////////////////////
    // Class Member Variables //
    ////////////////////////////
    private GraphView graph;
    private PointsGraphSeries series;
    private String graphType;
    private int numPoints;
    private int color;
    private boolean zoomable;

    ///////////////
    // Constants //
    ///////////////
    private static final int MAX_DATA_POINTS = 120;

    /*
     *  Constructor for a live line graph. Allows the graph to have points added to it dynamically.
     *  To add points to the graph, use the addPoints method.
     */
    public LineGraph(GraphView id, String title, String xAxis, String yAxis, int color)
    {

        GridLabelRenderer gridLabel;

        // Assign the graph to an interface ID
        graph = id;

        // Set the title of the graph
        graph.setTitle(title);
        graph.setTitleTextSize(48);

        // Set the x and y axis labels of the graph
        gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle(xAxis);
        gridLabel.setVerticalAxisTitle(yAxis);
        gridLabel.setLabelHorizontalHeight(10);
        gridLabel.setLabelVerticalWidth(30);

        // Initialize a new series of data points
        series = new PointsGraphSeries<>(new DataPoint[] {});

        // Assign the graph colors
        series.setColor(color);
        series.setSize(5);
        graph.setTitleColor(color);

        // Assign the new empty series to the graph
        graph.addSeries(series);

        // Initialize the number of points in the series
        numPoints = 0;

        // Declare that we want to manually set x and y bounds for the graph
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);

        // By default, dynamic graphs have zooming disabled
        this.disableZoom();
        zoomable = false;

        // Specify that the graph is dynamic, and can have points added to it
        graphType = "dynamic";

    }

    /*
     *  Constructor for a static line graph. Does not allow points to be added to it dynamically.
     *  Instead, takes in a file string, specifying points to add to the graph. The file string
     *  should be formatted in the following way:
     *
     *  <x-value>,<y-value>
     *  <x-value>,<y-value>
     *  <x-value>,<y-value>
     *
     *  ... and so on.
     */
    public LineGraph(GraphView id, String title, String xAxis, String yAxis, int color,
                     String fileContent)
    {

        GridLabelRenderer gridLabel;
        String[] points;
        float time;
        float value;

        // Assign the graph to an interface ID
        graph = id;

        // Clear the graph or any previously added series. This is necessary for places like the
        // view screen, where a single graph may be redrawn with a new data set and regression line
        // multiple times.
        graph.removeAllSeries();

        // Set the title of the graph
        graph.setTitle(title);
        graph.setTitleTextSize(48);

        // Set the x and y axis labels of the graph
        gridLabel = graph.getGridLabelRenderer();
        gridLabel.setHorizontalAxisTitle(xAxis);
        gridLabel.setVerticalAxisTitle(yAxis);

        // Initialize a new series of data points
        series = new PointsGraphSeries<>(new DataPoint[] {});

        // Assign the graph colors
        series.setColor(color);
        series.setSize(5);
        graph.setTitleColor(color);

        // Make sure a data set was recorded before attempting to parse values. If the data set is
        // empty, then don't do any further activity.
        if (!fileContent.equals(""))
        {

            // Split the file contents by line
            points = fileContent.split("\n");

            // Loop through each line
            for (String point : points)
            {

                // Parse out the seconds and value
                time = Float.parseFloat(point.split(",")[0]);
                value = Float.parseFloat(point.split(",")[1]);

                // Add the new data point to the series
                series.appendData(new DataPoint(time, value), false, points.length);

            }

            // After we are finished adding points to the series, add the series to the graph
            graph.addSeries(series);

            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setXAxisBoundsManual(true);

            // Set the Y axis range
            graph.getViewport().setMaxY(series.getHighestValueY());
            graph.getViewport().setMinY(series.getLowestValueY());

            // Set the X axis range
            graph.getViewport().setMaxX(series.getHighestValueX());
            graph.getViewport().setMinX(series.getLowestValueX());

            // By default, static graphs have zooming enabled
            this.enableZoom();

        }

        // Specify that the graph is static, and cannot have points dynamically added to it
        graphType = "static";

        // Save the color so that it can be used in the regression line
        this.color = color;

    }

    /*
     *  Adds a point to a dynamic graph at the specified time and value. If the graph has been
     *  initialized as a static graph, then the method does not add the point.
     */
    public void addPoint(float value, float time)
    {

        double lowest;
        double highest;
        double difference;
        double max;
        double min;

        // If the graph is static, and not dynamic, do not add the point
        if (graphType.equals("static"))
        {
            return;
        }

        // Add the point to the series
        series.appendData(new DataPoint(time, value), false, MAX_DATA_POINTS);

        // Increment the total number of points
        numPoints++;

        lowest = series.getLowestValueY();
        highest = series.getHighestValueY();
        difference = highest - lowest;

        // Only execute the following code if the graph is not currently zoomable
        if (!zoomable)
        {

            // If the total number of points is greater than the max allowed in the series, update
            // the minimum X value to show the lowest number still stored in the series
            if (numPoints > MAX_DATA_POINTS)
            {
                graph.getViewport().setMinX(series.getLowestValueX());
            }

            // Scale the viewport to a little more than the highest Y value, and a little less than
            // the lowest y value
            max = highest + (difference / 2);
            min = lowest - (difference / 2);

            if (min < 0)
            {
                min = 0;
            }

            graph.getViewport().setMinY(min);
            graph.getViewport().setMaxY(max);

            // Update the viewport to show the new maximum time
            graph.getViewport().setMaxX(time);
        }


    }

    /*
     *  Resets the graph. This simply removes all previously saved data points, and resets the
     *  points counter.
     */
    public void reset()
    {

        // Reset points counter and reinitialize the data series
        series.resetData(new DataPoint[] {});
        numPoints = 0;

    }

    /*
     *  On the graph screen, this method is called when the "Enable Zoom" button is pressed. Simply
     *  tells the graph that it is now in zoomable mode, and adjust the required settings to make
     *  it zoomable.
     */
    public void enableZoom()
    {

        // Toggle the class member variable which keeps track if the graph is currently zoomable
        zoomable = true;

        // Call the graph methods to make it zoomable
        graph.getViewport().setScalable(false);
        graph.getViewport().setScalableY(true);

    }

    /*
     *  On the graph screen, this method is called when the "Disable Zoom" button is pressed. Simply
     *  tells the graph that it is now not in zoomable mode, and adjust the required settings to
     *  no longer make it zoomable.
     */
    public void disableZoom()
    {

        // Toggle the class member variable which keeps track if the graph is currently zoomable
        zoomable = false;

        // Call the methods to make it no longer zoomable
        graph.getViewport().setScalable(false);
        graph.getViewport().setScalableY(false);

    }

    /*
     *  Adds a regression line to the graph using a y intercept and a slope. Currently uses the same
     *  color as the points on the graph.
     */
    public void addRegLine(double yIntercept, double slope)
    {

        double xStart;
        double xEnd;
        double yStart;
        double yEnd;
        LineGraphSeries regSeries;

        // Get the start and end points of the series
        xStart = series.getLowestValueX();
        xEnd = series.getHighestValueX();

        // Calculate the y values for those start and end points
        yStart = (slope * xStart) + yIntercept;
        yEnd = (slope * xEnd) + yIntercept;

        // Initialize the regression line series
        regSeries = new LineGraphSeries<>(new DataPoint[] {});
        regSeries.setColor(Color.argb(255, 255, 0, 0));
        regSeries.setThickness(3);

        // Add the calculated points to the regression line series.
        regSeries.appendData(new DataPoint(xStart, yStart), false, MAX_DATA_POINTS);
        regSeries.appendData(new DataPoint(xEnd, yEnd), false, MAX_DATA_POINTS);

        // Add the series to the graph
        graph.addSeries(regSeries);

    }

    public void resetZoom()
    {

        double min;
        double max;
        double difference;

        min = series.getLowestValueY();
        max = series.getHighestValueY();
        difference = max - min;

        max = max + (difference / 2);
        min = min - (difference / 2);

        graph.getViewport().setMaxY(max);
        graph.getViewport().setMinY(min);
        graph.getViewport().setMaxX(series.getHighestValueX());
        graph.getViewport().setMinX(series.getLowestValueX());
    }

}


