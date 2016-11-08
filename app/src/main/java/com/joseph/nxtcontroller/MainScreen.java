package com.joseph.nxtcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainScreen extends AppCompatActivity {

    BluetoothAdapter BA;
    Set<BluetoothDevice> pairedDevices;
    Button connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        connectBtn = (Button)findViewById(R.id.vv_btnConnect);
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA.isEnabled()){
            Toast.makeText(getApplicationContext(), "Bluetooth is enabled, attempting to connect",Toast.LENGTH_LONG).show();
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }
        else {
            Toast.makeText(getApplicationContext(), "Bluetooth is disabled, please turn on",Toast.LENGTH_LONG).show();
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discover();
            }
        });


    }


    public void discover(){

    }

    public void list(){

    }


}
