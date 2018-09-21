package edu.nau.li_840a_interface;

import java.util.Set;
        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.AdapterView.OnItemClickListener;
        import android.widget.ArrayAdapter;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

 public class BTSelectDevice  extends Activity {

     boolean connecting=false;

     // Member fields
     private BluetoothAdapter mBtAdapter;
     private ArrayAdapter<String> mPairedDevicesArrayAdapter;

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.bt_device_list);
     }

     @Override
     public void onResume()
     {
         super.onResume();
         checkBTState();
     }

     private void checkBTState() {
         // Check device has Bluetooth and that it is turned on
         mBtAdapter=BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
         if(mBtAdapter==null) { // NO BLUETOOTH SUPPORTED
             Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
             Intent intent=new Intent();
             setResult(RESULT_CANCELED, intent);
             finish();
             return;
         } else {
             if (!mBtAdapter.isEnabled()) {
                 //Prompt user to turn on Bluetooth
                 Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                 startActivityForResult(enableBtIntent, 1);
             } else {deviceselection();}
         }
         }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode==1){
             if (resultCode==RESULT_OK) {
                 deviceselection();
             } else {
                 Toast.makeText(getBaseContext(), "You need to grant Bluetooth permissions in order to use Bluetooth", Toast.LENGTH_SHORT).show();
                 Intent intent=new Intent();
                 setResult(RESULT_CANCELED, intent);
                 finish();
                 return;}

         }
     }


     public void deviceselection(){

         // Initialize array adapter for paired devices
         mPairedDevicesArrayAdapter = new ArrayAdapter<>(this,  android.R.layout.simple_list_item_1);

         // Find and set up the ListView for paired devices
         ListView pairedListView = findViewById(R.id.paired_devices);
         pairedListView.setAdapter(mPairedDevicesArrayAdapter);
         pairedListView.setOnItemClickListener(mDeviceClickListener);

         // Get the local Bluetooth adapter
         mBtAdapter = BluetoothAdapter.getDefaultAdapter();

         // Get a set of currently paired devices and append to 'pairedDevices'
         Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

         // Add previosuly paired devices to the array
         if (pairedDevices.size() > 0) {
             findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
             for (BluetoothDevice device : pairedDevices) {
                 mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
             }
         } else {
             String noDevices = "No devices have been paired";// getResources().getText(R.string.none_paired).toString();
             mPairedDevicesArrayAdapter.add(noDevices);
         }
     }

     // Set up on-click listener for the list (nicked this - unsure)
     private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
         public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
             if (connecting) return;
             connecting=true;
             // Get the device MAC address, which is the last 17 chars in the View
             String info = ((TextView) v).getText().toString();
             String address = info.substring(info.length() - 17);
             ((TextView) v).setText(info +"\n" + "connecting...");
             ((TextView) v).setBackgroundColor(0xff99cc00);
             // Make an intent to start next activity while taking an extra which is the MAC address.
             Intent intent=new Intent();
             intent.putExtra("DEVICE_ADDRESS",address);
             setResult(RESULT_OK, intent);
             finish();
         }
     };

 }