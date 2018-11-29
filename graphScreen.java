package edu.nau.li_840a_interface;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Set;

import static java.lang.Double.NaN;

public class graphScreen extends AppCompatActivity {

    private GraphView graphIds[];
    private TextView textIds[];
    private GraphManager manager;
    private Thread manager_thread;
    private SerialReader reader;

    // CONNECTION CONSTANTS
    public static int NOT_CONNECTED = 0;  // Where?
    public static int USB = 1;  // Where?
    public static int BLUETOOTH = 2;  // Where?
    // CONNECTION STATUS
    public static int device_connection=NOT_CONNECTED; // Is a Sensor connected?
    public static boolean bt_established=false;
    public static int SELECT_BT_DEVICE = 1;  // Intent_response id
    // COUNTDOWN TIMER
    public static int logduration=300000;//ms; default setting
    public static String countdown_label;
    public static  boolean loggingstopped =false;

    /*
     *  Constructor for the graph screen. Fetches the screen IDs for the graphs and text boxes, and
     *  passes them off to the GraphManager to be updated at a rate of once per second.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initializes the display
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_screen);

        // Sets the screen in fullscreen mode
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

        // Get the screen IDs for each of the four graphs
        graphIds = new GraphView[4];
        graphIds[0] = findViewById(R.id.graph1);
        graphIds[1] = findViewById(R.id.graph2);
        graphIds[2] = findViewById(R.id.graph3);
        graphIds[3] = findViewById(R.id.graph4);

        // Get the screen IDs for each of the four text views
        textIds = new TextView[9];
        textIds[0] = findViewById(R.id.co2display);
        textIds[1] = findViewById(R.id.h2odisplay);
        textIds[2] = findViewById(R.id.tempdisplay);
        textIds[3] = findViewById(R.id.presdisplay);

        textIds[4] = findViewById(R.id.sitedisplay);
        textIds[5] = findViewById(R.id.sampledisplay);
        textIds[6] = findViewById(R.id.instrumentdisplay);
        textIds[7] = findViewById(R.id.finalbutton);
        textIds[8] = findViewById(R.id.warningdisplay);

        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        if(bd != null)
        {
            String getName = (String) bd.get("SITE_NAME");
            textIds[4].setText(getName);
            getName = (String) bd.get("SAMPLE_ID");
            textIds[5].setText(getName);
        }
        countdown_label= String.format("Log duration\n%d:%02d", logduration/ (60 * 1000) % 60, logduration / 1000 % 60);
        textIds[7].setText(countdown_label);

        // Initialize the handler. This is used for the USB/BT serial communication
        // Bluetooth and USB support... we cativate both services and switch between them ... the unused one will just be be idling in background
        mbtHandler = new graphScreen.MyBTHandler(this);
        musbHandler = new graphScreen.MyUSBHandler(this);

        // Initialize a new serial reader object, for reading in information from the instrument
        reader = new SerialReader();

        // Initialize a new graph manager using the graph and text IDs, and then set it to start
        // updating on its own thread
        manager = new GraphManager(this, graphIds, textIds);
        manager_thread = new Thread(manager);
        manager_thread.start();
        // We need initial user input to find the sensors on USB or BLUETOOTH
        if (device_connection==NOT_CONNECTED) {connectDevice();}else { setConnected(device_connection);}
    }

    /*
     *  Runs when the "Connect/Disconnect" button is pressed
     */
    public void connectDeviceToggle(View view)
    {
        if (device_connection>NOT_CONNECTED){
            setNotConnected();
        }
        else{
            connectDevice();
        }
    }

    public void setNotConnected(){
        final Button button = findViewById(R.id.connectbutton);
        button.setText("Connect");
        device_connection=NOT_CONNECTED;
        bt_established=false;
    }

    public void setConnected(int connection){
        final Button button = findViewById(R.id.connectbutton);
        if (connection==BLUETOOTH){
            button.setText("Disconnect Bluetooth");
            device_connection=BLUETOOTH;
            bt_established=true;
        }
        if (connection==USB){
            button.setText("Disconnect USB");
            device_connection=USB;
        }
    }

    public void connectDevice() {
        // Let's find out some properties of the Android device...
        // for now let's just assume the Android device supports USB and Bluetooth, it throws an error later if not supported
        final Context  con=this;

        // Initialize the alert box
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        // Set the title and message of the alert box
        builder.setTitle("Puppy not found");
        builder.setMessage("How would you like to take your puppy for a walk?\n(Connect the device)");

        // Set the icon of the alert box
        Resources res = getResources();
        builder.setIcon(res.getDrawable(R.drawable.dog));


        builder.setNeutralButton("On a test leash\n(Data Mocker)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Data Mocker Functionality
                manager.updateData("I like candy");


            }
        });

        // If the yes button is pressed
        builder.setPositiveButton("On a leash\n(USB)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                setConnected(USB);

                unregisterReceiver(sensorReceiver);
                setFilters();  // Start listening notifications from USB Services
                startUSBService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
                Intent intent = new Intent(UsbService.ACTION_USB_INITIALIZE);  // Test if USB connection is available... if not the service will broadcast an error and inactivate connection
                sendBroadcast(intent);
            }
        });

        builder.setNegativeButton("Unleashed\n(Bluetooth)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                device_connection = BLUETOOTH;
                bt_established=false;
                unregisterReceiver(sensorReceiver);
                setFilters();
                startBTService(BTService.class, btConnection, null); // Start BTService(if it was not started before) and Bind it
                // -- User needs to select the correct BT device --
                Intent selectBTdevice = new Intent(con, BTSelectDevice.class);
                startActivityForResult(selectBTdevice, SELECT_BT_DEVICE);
            }
        });
        // Show the alert box
        builder.show();
    }

    // FOR BLUETOOTH: If user has selected Bluetooth, we need to know the address of the device to connect.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            setNotConnected();
        } else {
            if (requestCode == SELECT_BT_DEVICE) {
                BTService.address = data.getStringExtra("DEVICE_ADDRESS");
                btService.BTconnect();
                setConnected(BLUETOOTH);
            }
        }
    }
    /*
     *  Runs when the "CO2" button is pressed. Hides every graph except for the CO2 one.
     */
    public void showCO2(View view)
    {

        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(0, 0));


    }

    /*
     *  Runs when the "H2O" button is pressed. Hides every graph except for the H2O one.
     */
    public void showH2O(View view)
    {

        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(0, 0));

    }

    /*
     *  Runs when the "Temp" button is pressed. Hides every graph except for the temperature one.
     */
    public void showTemp(View view)
    {

        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(0, 0));

    }

    /*
     *  Runs when the "Pressure" button is pressed. Hides every graph except for the pressure one.
     */
    public void showPres(View view)
    {

        graphIds[3].setLayoutParams(new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT));
        graphIds[0].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[1].setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        graphIds[2].setLayoutParams(new LinearLayout.LayoutParams(0, 0));

    }

    /*
     *  Runs when either the "Start Logging" or "Stop Logging" buttons are pressed. Switches the
     *  text to the appropriate value and tells the graph manager to either start or stop recording
     *  incoming data points.
     */
    public void startStopLog(View view)
    {

        final Context con = this;

        final Button button = findViewById(R.id.logbutton);
        final Button finalizeButton = findViewById(R.id.finalbutton);

        // If the button current says "Start Logging", switch the text and inform the manager
        if (button.getText().equals("Start Logging"))
        {

            if (!manager.isEmpty())
            {
                // Initialize the alert box
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(this);

                // Set the title and message of the alert box
                builder.setTitle("Overwrite previous data set?");
                builder.setMessage("Are you sure you want to overwrite the previously collected" +
                        " data set?");

                // Set the icon of the alert box
                builder.setIcon(android.R.drawable.ic_dialog_alert);

                // If the yes button is pressed
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        button.setText("Stop Logging");
                        finalizeButton.setBackgroundColor(Color.TRANSPARENT);
                        finalizeButton.setEnabled(false);
                        finalizeButton.setTextSize(24);
                        finalizeButton.setTextColor(0xff99cc00);
                        manager.resetGraphs();
                        manager.startlogging(logduration);
                        manager.countdownNotified=false;

                    }
                });

                // If the no button is pressed, do nothing
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                // Show the alert box
                builder.show();
            }
            else
            {

                button.setText("Stop Logging");
                finalizeButton.setBackgroundColor(Color.TRANSPARENT);
                finalizeButton.setEnabled(false);
                finalizeButton.setTextSize(24);
                finalizeButton.setTextColor(0xff99cc00);

                manager.resetGraphs();
                manager.startlogging(logduration);
            }

        }

        // If the button currently says "Stop Logging", switch the text and inform the manager
        else
        {
            button.setText("Start Logging");
            if (!manager.isEmpty())
            {
                finalizeButton.setBackgroundResource(android.R.drawable.btn_default);
                finalizeButton.setEnabled(true);
                finalizeButton.setText("Save and Exit");
                finalizeButton.setTextSize(14);
                finalizeButton.setTextColor(button.getCurrentTextColor());
                loggingstopped=true;
            } else {
                countdown_label= String.format("Log duration\n%d:%02d", logduration/ (60 * 1000) % 60, logduration / 1000 % 60);
                finalizeButton.setBackgroundResource(android.R.drawable.btn_default);
                finalizeButton.setEnabled(true);
                finalizeButton.setText(countdown_label);
                finalizeButton.setTextSize(14);
                finalizeButton.setTextColor(button.getCurrentTextColor());
                loggingstopped=true;
            }
            manager.stoplogging();
        }

    }

    /*
     *  Runs when the zoom button is pressed. If it currently says "Enable Zoom", then it tells
     *  the graph manager to enable zoom for each graph. If it current says "Disable Zoom", then it
     *  tells the graph manager to disable zoom for each graph.
     */
    public void zoomToggle(View view)
    {

        Button button = findViewById(R.id.zooombutton);

        // If the button currently says "Enable Zoom"
        if (button.getText().equals("Enable Zoom"))
        {
            button.setText("Disable Zoom");
            manager.enableZoom();
        }

        // If the button currently says "Disable Zoom"
        else
        {
            button.setText("Enable Zoom");
            manager.disableZoom();
        }
    }

    /*
     *  Runs when the "Back" button is pressed. Simply goes back to the metadata screen without
     *  witting any files.
     */
    public void goToMetadata(View view)
    {

        final Context con = this;

        // Initialize the alert box
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);

        // Set the title and message of the alert box
        builder.setTitle("Exit graph screen?");
        builder.setMessage("Are you sure you want to exit the graph screen? You will lose all" +
                " currently recorded data.");

        // Set the icon of the alert box
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        // If the yes button is pressed
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Stop manager
                manager.stoplogging();
                manager.deconstruct();
                loggingstopped=false; // Reset logging status
                // Initialize the meta data screen
                Intent metaDataScreen;
                metaDataScreen = new Intent(con, metaData.class);

                // JIMMY DO NOT DELETE THIS
                // Fetch the previously entered meta data IMAGE
                Bitmap metaImage = getIntent().getParcelableExtra("IMAGE");
                metaDataScreen.putExtra("FLAG", "True");
                metaDataScreen.putExtra("IMAGE", metaImage);

                // Start the new screen
                startActivity(metaDataScreen);

            }
        });

        // If the no button is pressed, do nothing
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        // Show the alert box
        builder.show();
    }

    /*
     *  Runs when the "Finalize" button is pressed. Gets the string values for the graph file,
     *  metadata file, and image file, writes them to files, deconstructs the graph manager, and
     *  then takes the user to the file directory.
     */
    public void finalize(View view)
    {
    if (!loggingstopped ) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Log duration");
        alert.setMessage("Intended log duration (in seconds):");

        // Set an EditText view to get user input
        final EditText seconds = new EditText(this);
        seconds.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        seconds.setMaxLines(1);
        seconds.setImeOptions(EditorInfo.IME_ACTION_DONE);
        alert.setView(seconds);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = seconds.getText().toString();
                if (!value.equals("")) {
                    logduration = Integer.parseInt(value) * 1000;
                }
                countdown_label = String.format("Log duration\n%d:%02d", logduration / (60 * 1000) % 60, logduration / 1000 % 60);
                textIds[7].setText(countdown_label);
                return;
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        return;
                    }
                });
        alert.show();

     return;
    }
        loggingstopped=false;

        String reading;
        String metaInstrument;
        String metaSite;
        String metaOpName;
        String metaSampleId;
        String metaTemp;
        String metaComments;
        String metaTime;
        String metaString;
        String metaLong;
        String metaLat;
        String metaElevation;
        Bitmap imageString;
        String fileName;
        String graphArray[];
        String rSquared;
        String stdError;
        String regSlope;
        FileOutputStream outStream;
        Intent fileScreen;
        Double firstSecond;
        Double lastSecond;

        // Set the button text to "Saving..."
        Button button = findViewById(R.id.finalbutton);
        button.setText("Saving...");

        // Get the actual startTime from the manager
        metaTime=manager.startTimeFormatted;

        // Deconstruct the manager
        manager.stoplogging();
        manager.deconstruct();
        reader.communicating = false;

        // Get the graph CSV from the manager
        reading = manager.toString();

        // Fetch the metadata values
        metaInstrument = manager.instrument;
        metaSite = getIntent().getStringExtra("SITE_NAME");
        metaOpName = getIntent().getStringExtra("OPERATOR_NAME");
        metaSampleId = getIntent().getStringExtra("SAMPLE_ID");
        metaTemp = getIntent().getStringExtra("TEMPERATURE");
        metaComments = getIntent().getStringExtra("COMMENTS");
        //metaTime = getIntent().getStringExtra("TIME");
        metaLong = getIntent().getStringExtra("GPSLong");
        metaLat = getIntent().getStringExtra("GPSLat");
        metaElevation = getIntent().getStringExtra("ELEVATION");
        imageString = getIntent().getParcelableExtra("IMAGE");

        // Split the graph data so that we can only get stats on the CO2 graph
        graphArray = splitGraphData(reading);

        // Calculate each of the stats
        DecimalFormat df = new DecimalFormat("#.0000");
        rSquared = df.format(getRSquared(graphArray[0]));
        stdError = df.format(getStandardError(graphArray[0]));
        regSlope = df.format(getRegressionSlope(graphArray[0]));

        try
        {

            firstSecond = getXRangeStart(graphArray[0]);  // we now reset the counter when logging starts
            lastSecond = getXRangeEnd(graphArray[0]);

        }
        catch (Exception e)
        {

            firstSecond = NaN;
            lastSecond = NaN;

        }

        // Construct the CSV file content
        metaString = "Instrument,Operator Name,Site Name,Sample ID,Temperature,Comments,Time and Date,Longitude," +
                     "Latitude,Elevation,R Squared,Regression Slope,Standard Error,X Start Range,X End Range,AppVersion\n" +
                     metaInstrument + "," +metaOpName + "," + metaSite + "," + metaSampleId + "," + metaTemp + "," +
                     metaComments + "," + metaTime + "," + metaLong + "," + metaLat + "," +
                     metaElevation + "," + rSquared + "," + regSlope + "," + stdError + "," +
                df.format(firstSecond) + "," + df.format(lastSecond) + "," + getString(R.string.version_name);


        // Build the file name using the site name, sample id, and time stamp
        fileName = metaSite + "_" + metaSampleId + "_" + metaTime;

        // In case any file I/O exception happen, we write this in a try/catch block
        try
        {
            // Graph File
            outStream = openFileOutput("G-" + fileName + ".csv", Context.MODE_APPEND);
            outStream.write(reading.getBytes());
            outStream.close();

            // Metadata File
            outStream = openFileOutput("M-" + fileName + ".csv", Context.MODE_APPEND);
            outStream.write(metaString.getBytes());
            outStream.close();

            // Image File, only if an image was taken
            if (imageString != null)
            {
                outStream = openFileOutput("I-" + fileName + ".png", Context.MODE_APPEND);
                //Bitmap imageBMP = BitmapFactory.decodeFile(metaImageFile);
                imageString.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                //outStream.write(imageString.getBytes());
                outStream.close();
            }

        }
        catch(Exception exception)
        {

        }

        // Initialize a new file screen and take the user there
        fileScreen = new Intent(this, fileDirectory.class);
        fileScreen.putExtra("ND_BUTTON", "True");

        startActivity(fileScreen);

    }


    /////////////////////
    // USB SERIAL CODE //
    //  DO NOT MODIFY  // WELL, I DID IT ANYWAY /DBASLER 2018-05-18
    /////////////////////

    /*
     * Notifications from UsbService OR Bluetooth Service will be received here.
     */

    private final BroadcastReceiver sensorReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (device_connection == BLUETOOTH) {
                switch (intent.getAction()) {
                    case BTService.ACTION_BT_READY:
                        setConnected(BLUETOOTH);
                        break;
                    case BTService.ACTION_BT_SOCKETERROR:
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        break;
                    case BTService.ACTION_BT_SOCKETCLOSED:
                        Toast.makeText(getBaseContext(), "Connection Failure: Socket closed", Toast.LENGTH_SHORT).show();
                        break;
                    case BTService.ACTION_BT_NOTSUPPORTED:
                        Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                        break;
                }
                    // ADD BT_BROADCASTS HERE
            }
            else if (device_connection == USB)  {
                switch (intent.getAction()) {
                    case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                        Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                        if (usbService != null) {
                            String initMessage;
                            initMessage = "<CFG><OUTRATE>0.5</OUTRATE></CFG>\n";
                            usbService.write(initMessage.getBytes());
                        }
                        break;
                    case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                        Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                        setNotConnected();
                        break;
                    case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                        Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                        setNotConnected();
                        break;
                    case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                        Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                        setNotConnected();
                        break;
                    case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                        Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                        setNotConnected();
                        break;
                }
            }
        }
    };

    private BTService btService;
    private UsbService usbService;

  //  private graphScreen.MyHandler mHandler;
  private graphScreen.MyBTHandler mbtHandler;
  private graphScreen.MyUSBHandler musbHandler;


    private final ServiceConnection btConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                btService = ((BTService.BTBinder) arg1).getService();
                btService.setHandler(mbtHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            btService = null;
        }
    };


    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                usbService = ((UsbService.UsbBinder) arg1).getService();
                usbService.setHandler(musbHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from Services
        startBTService(BTService.class, btConnection, null);  // Start BTService(if it was not started before) and Bind it
        startUSBService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        if (device_connection >0 && bt_established){
            unregisterReceiver(sensorReceiver);
            unbindService(btConnection);
            unbindService(usbConnection);

        }
    }

    private void startBTService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!BTService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startUSBService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED ) {
                Intent startService = new Intent(this, service);
                if (extras != null && !extras.isEmpty()) {
                    Set<String> keys = extras.keySet();
                    for (String key : keys) {
                        String extra = extras.getString(key);
                        startService.putExtra(key, extra);
                    }
                }
                startService(startService);
            }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        if (device_connection == BLUETOOTH) {
            filter.addAction(BTService.ACTION_BT_READY);
            filter.addAction(BTService.ACTION_BT_SOCKETERROR);
            filter.addAction(BTService.ACTION_BT_SOCKETCLOSED);
            filter.addAction(BTService.ACTION_BT_NOTSUPPORTED);
        } else if (device_connection == USB)  {
            filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
            filter.addAction(UsbService.ACTION_NO_USB);
            filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
            filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
            filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        }
        registerReceiver(sensorReceiver, filter);
    }


    private static class MyBTHandler extends Handler {
        private final WeakReference<graphScreen> mActivity;
        public MyBTHandler(graphScreen activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            if (device_connection == BLUETOOTH) {
                switch (msg.what) {
                    case BTService.MESSAGE_FROM_SERIAL_PORT:
                        String data = (String) msg.obj;
                        mActivity.get().reader.addChar(data);
                        break;
                }
            }
        }
    }

    private static class MyUSBHandler extends Handler {
        private final WeakReference<graphScreen> mActivity;
        public MyUSBHandler(graphScreen activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            if (device_connection == USB)  {
                switch (msg.what) {
                    case UsbService.MESSAGE_FROM_SERIAL_PORT:
                        String data = (String) msg.obj;
                        mActivity.get().reader.addChar(data);
                        break;
                    case UsbService.CTS_CHANGE:
                        Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                        break;
                    case UsbService.DSR_CHANGE:
                        Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    }


    ////////////////////////////
    // END OF USB SERIAL CODE //
    ////////////////////////////

    /////////////////////
    // PRIVATE CLASSES //
    /////////////////////

    /*
     *  Class used to communicate with the USB serial connection. More or less, it is just a really
     *  glorified string builder.
     */
    private class SerialReader
    {

        public String currentStream;
        public String completeStream;
        public boolean communicating;

        /*
         *  Constructor for the serial reader. Initializes all the class member variables.
         */
        public SerialReader()
        {
            currentStream = "";
            completeStream = "";
            communicating = true;
        }

        /*
         *  This method is called every time the USB port picks up a piece of data from the
         *  instrument. Makes sure it is not empty or a space, then adds it to the string being
         *  built. If the message contains a line break, then we know it is the end of the current
         *  message.
         */
        public void addChar(String input)
        {

            // Check to make sure its not a space or empty
            if (input.equals("") || input.equals(" "))
            {
                return;
            }

            // If it contains a line break, then we know we have reached the end of the current
            // message. Send the complete message to the graph manager.
            if (input.contains("\n"))
            {
                currentStream += input;
                completeStream = currentStream.trim();
                currentStream = "";
                // Toast.makeText(getBaseContext(),completeStream, Toast.LENGTH_LONG).show(); //DEBUG ONLY
                manager.updateData(completeStream);
            }

            // If it does not contain a line break, then we just add the message to the string
            // being built.
            else
            {
                currentStream += input;
            }

        }

    }

    /*
     * If the Android back button is pressed, we don't want anything to happen since all of our
     * app nagivation is handled by on screen buttons.
     */
    @Override
    public void onBackPressed()
    {

    }

    // JOEY ADDED STATISTICS HERE TO BE ABLE TO WRITE TO THE CSV FILE
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

            co2Points += values[0] + "," + values[7] + "\n";
            h2oPoints += values[0] + "," + values[8] + "\n";
            tempPoints += values[0] + "," + values[9] + "\n";
            presPoints += values[0] + "," + values[10] + "\n";

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

        if (graphPoints.equals("")){
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

        if (graphPoints.equals("")){
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
    private double getXRangeStart(String graphPoints) {

        String[] tempData;
        String[] firstData;
        double firstSecond;

        //split data
        tempData = graphPoints.split("\n");

        firstData = tempData[0].split(",");
        firstSecond = Float.parseFloat(firstData[0]);

        return firstSecond;
    }

    private double getXRangeEnd(String graphPoints) {

        String[] tempData;
        String[] lastData;
        double lastSecond;

        //split data
        tempData = graphPoints.split("\n");

        lastData = tempData[tempData.length - 1].split(",");
        lastSecond = Float.parseFloat(lastData[0]);

        return lastSecond;
    }
    // END OF JOEYS CODE ADDING

}
