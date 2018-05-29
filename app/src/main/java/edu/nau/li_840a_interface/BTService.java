package edu.nau.li_840a_interface;

import android.app.Service;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;


public class BTService extends Service{
    public static final String ACTION_BT_READY = "edu.nau.li_840a_interface.ACTION_BT_READY";
    public static final String ACTION_BT_SOCKETERROR = "edu.nau.li_840a_interface.ACTION_BT_SOCKETERROR";
    public static final String ACTION_BT_SOCKETCLOSED = "edu.nau.li_840a_interface.ACTION_BT_SOCKETCLOSED";
    public static final String ACTION_BT_NOTSUPPORTED = "edu.nau.li_840a_interface.ACTION_BT_NOTSUPPORTED";

    public static boolean SERVICE_CONNECTED = false;
    private Handler bluetoothIn;
    private IBinder binder = new BTBinder();
    private Context context;
    public static final int MESSAGE_FROM_SERIAL_PORT = 0;                        //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    public static String address="no_address";

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onCreate() {
        this.context = this;
        BTService.SERVICE_CONNECTED = true;
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BTdisconnect();
    }

    public class BTBinder extends Binder {
        public BTService getService() {
            return BTService.this;
        }
    }

    public void setHandler(Handler mHandler) {
        this.bluetoothIn = mHandler;
    }

    public void BTdisconnect() {
        try
        {
            btSocket.close(); //Don't leave Bluetooth sockets open when leaving activity
        } catch (IOException e2) {
            //insert code to deal with this
        }
        BTService.SERVICE_CONNECTED = false;
    }

    public void BTconnect() {
        if (address.equals("no_address")) return;
        //Get MAC address from bt_device_list Activity started from button on graphic screen
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Intent intent = new Intent(ACTION_BT_SOCKETERROR);
            context.sendBroadcast(intent);
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                Intent intent = new Intent(ACTION_BT_SOCKETERROR);
                context.sendBroadcast(intent);
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }


    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        if(btAdapter==null) {
            Intent intent = new Intent(ACTION_BT_NOTSUPPORTED);
            context.sendBroadcast(intent);
        }
        // REQUEST FOR PERMISSION IS HANDLED ELSEWHERE (SELECT DEVICE)
    }

        //create new class for connect thread
        private class ConnectedThread extends Thread {
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            //creation of the connect thread
            public ConnectedThread(BluetoothSocket socket) {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                try {
                    //Create I/O streams for connection
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                byte[] buffer = new byte[256];
                int bytes;
                // Keep looping to listen for received messages
                while (true) {
                    try {
                        bytes = mmInStream.read(buffer);            //read bytes from input buffer
                        String readMessage = new String(buffer, 0, bytes);
                        // Send the obtained bytes to the UI Activity via handler
                        bluetoothIn.obtainMessage(MESSAGE_FROM_SERIAL_PORT, bytes, -1, readMessage).sendToTarget();
                    } catch (IOException e) {
                        break;
                    }
                }
            }

            //write method (Not Actively used in this application.... merely as a test if connection is established.
            // That said, the command <li840>?</li840> would yield a long list of configuration parameters of the device
            public void write(String input) {
                byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
                try {
                    mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                } catch (IOException e) {
                    //if you cannot write, close the application
                    Intent intent = new Intent(ACTION_BT_SOCKETCLOSED);
                    context.sendBroadcast(intent);
                    return;
                }
                // Everything went as expected. Send an intent to MainActivity
                Intent intent = new Intent(ACTION_BT_READY);
                context.sendBroadcast(intent);

            }
        }
    }




