package com.joseph.nxtcontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MainScreen extends AppCompatActivity {


    private TextView mStatusTv;
    private Button mActivateBtn;
    private Button mConnectBtn;
    private Button mPairedBtn;
    private Button mScanBtn;
    private InputStream is;
    private OutputStream os;
    private ImageView icon;
    private TextView connectionStatus;
    private Button btnDrive;

    private ProgressDialog mProgressDlg;

    ArrayList<BluetoothDevice> mDeviceList;


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_screen);

        mStatusTv = (TextView) findViewById(R.id.tv_status);
        mActivateBtn = (Button) findViewById(R.id.vv_btnConnect);
        mConnectBtn = (Button) findViewById(R.id.connectRobot);
        mPairedBtn = (Button) findViewById(R.id.btn_view_paired);
        mScanBtn = (Button) findViewById(R.id.btn_scan);
        btnDrive = (Button)findViewById(R.id.btnDrive);
        icon = (ImageView)findViewById(R.id.vv_IcoImag);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectionStatus = (TextView)findViewById(R.id.vv_tvConnectStatus);


        mProgressDlg = new ProgressDialog(this);
        mProgressDlg.setMessage("Scanning...");
        mProgressDlg.setCancelable(false);
        mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
            }
        });

        if (mBluetoothAdapter == null) {
            showUnsupported();
        } else {
            mPairedBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //HashSet<BluetoothDevice> pairedDevices = (HashSet)mBluetoothAdapter.getBondedDevices();
                    HashSet<BluetoothDevice> pairedDevices = new HashSet<>();
                    pairedDevices.clear(); // clear old
                    for(BluetoothDevice d : mBluetoothAdapter.getBondedDevices()){ // checks for duplicates
                        if (!pairedDevices.contains(d)){
                            //ArrayList<BluetoothDevice> list = new ArrayList<>();
                            //list.addAll(pairedDevices);
                            //Intent intent = new Intent(MainScreen.this, DeviceListActivity.class);
                            //intent.putParcelableArrayListExtra("device.list", list);
                            //startActivity(intent);
                            pairedDevices.add(d);
                        }
                    }

                    if (pairedDevices == null || pairedDevices.size() == 0) {
                        showToast("No Paired Devices Found");
                    }

                    else {
                        ArrayList<BluetoothDevice> list = new ArrayList<>();
                        list.addAll(pairedDevices);
                        Intent intent = new Intent(MainScreen.this, DeviceListActivity.class);
                        intent.putParcelableArrayListExtra("device.list", list);
                        startActivity(intent);
                    }

                }
            });


            mConnectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    for(BluetoothDevice d : mBluetoothAdapter.getBondedDevices()) {

                        if (d.getName().equalsIgnoreCase("NXT04")){
                            showToast("Found robot, attempting to connect!");
                            connectToRobot(d);

                        }
                    }


                }
            });


            mScanBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    //mBluetoothAdapter.startDiscovery();
                    BluetoothAdapter.getDefaultAdapter().startDiscovery();
                }
            });

            mActivateBtn.setOnClickListener(new View.OnClickListener() { // change this, maybe
                @Override
                public void onClick(View v) {
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                        showDisabled();
                    } else {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent, 1000);
                    }
                }
            }); // change this

            btnDrive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveMotor(0, 75, 0x20);
                }
            });

            if (mBluetoothAdapter.isEnabled()) {
                showEnabled();
            } else {
                showDisabled();
            }
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        registerReceiver(mReceiver,new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
        registerReceiver(mReceiver,new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
    }

    @Override
    public void onPause() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }

        super.onPause();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    private void showConnected(){
        icon.setImageResource(R.drawable.bt_on);
    }

    private void showEnabled() {
        mStatusTv.setText("Bluetooth is On");
        mStatusTv.setTextColor(Color.BLUE);

        mActivateBtn.setText("Disable");
        mActivateBtn.setEnabled(true);

        mPairedBtn.setEnabled(true);
        mScanBtn.setEnabled(true);
    }

    private void showDisabled() {
        mStatusTv.setText("Bluetooth is Off");
        mStatusTv.setTextColor(Color.RED);
        icon.setImageResource(R.drawable.bt_off);

        mActivateBtn.setText("Enable");
        mActivateBtn.setEnabled(true);

        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
    }

    private void showUnsupported() {
        mStatusTv.setText("Bluetooth is unsupported by this device");

        mActivateBtn.setText("Enable");
        mActivateBtn.setEnabled(false);

        mPairedBtn.setEnabled(false);
        mScanBtn.setEnabled(false);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDeviceList.add(device);
                if (device.getName() == null){
                    showToast("Found device " + device.getAddress());
                }
                else {
                    showToast("Found device " + device.getName());
                }
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_ON) {
                    showToast("Enabled");
                    showEnabled();
                }
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mDeviceList = new ArrayList<>();
                mProgressDlg.show();
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mProgressDlg.dismiss();

                Intent newIntent = new Intent(MainScreen.this, DeviceListActivity.class);
                if (mDeviceList.isEmpty()) {
                    showToast("Device List: EMPTY");
                }
                newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
                startActivity(newIntent);
            }

            if (intent.getAction().equals("android.bluetooth.device.action.ACL_CONNECTED")) { // working
                try {
                    is = socket.getInputStream();
                    os = socket.getOutputStream();
                    showToast(("Connect to " + mBluetoothAdapter.getName() + " at " + mBluetoothAdapter.getAddress()));
                    connectionStatus.setText("Connect to " + mBluetoothAdapter.getName() + " at " + mBluetoothAdapter.getAddress());
                    //bConnected = true;
                    icon.setImageResource(R.drawable.bt_on);
                } catch (Exception e) {
                    is = null;
                    os = null;
                    //disconnectFromRobot(null);
                }
            }
            if (intent.getAction().equals("android.bluetooth.device.action.ACL_DISCONNECTED")) {
                showToast("Connected broken");
                connectionStatus.setText("Not connected");
            }

        }
    };

    private void connectToRobot(BluetoothDevice bd) {
        try {
            socket = bd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
            //showToast(("Connect to " + bd.getName() + " at " + bd.getAddress()));
            //connectionStatus.setText("Connect to " + bd.getName() + " at " + bd.getAddress());
        } catch (Exception e) {
            showToast("Error with remote device [" + e.getMessage() + "]");
        }
    }


    private void moveMotor(int motor,int speed, int state) {
        try {
            byte[] buffer = new byte[15];

            buffer[0] = (byte) (15-2);			//length lsb
            buffer[1] = 0;						// length msb
            buffer[2] =  0;						// direct command (with response)
            buffer[3] = 0x04;					// set output state
            buffer[4] = (byte) motor;			// output 1 (motor B)
            buffer[5] = (byte) speed;			// power
            buffer[6] = 1 + 2;					// motor on + brake between PWM
            buffer[7] = 0;						// regulation
            buffer[8] = 0;						// turn ration??
            buffer[9] = (byte) state; //0x20;					// run state
            buffer[10] = 0;
            buffer[11] = 0;
            buffer[12] = 0;
            buffer[13] = 0;
            buffer[14] = 0;

            os.write(buffer);
            os.flush();
        }
        catch (Exception e) {
            showToast("Error in MoveForward(" + e.getMessage() + ")");
        }
    }






}
