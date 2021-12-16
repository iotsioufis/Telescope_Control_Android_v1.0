package com.my_project.telescopecontrol;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.bluetooth.BluetoothDevice;

import static com.my_project.telescopecontrol.MainActivity.MESSAGE_WRITE;
import static com.my_project.telescopecontrol.MainActivity.handler;

public class BtBroadcastReceiver extends BroadcastReceiver {
    private final static int INITIAL = 0;
    private final static int CONNECTION_LOST = -1; // used in bluetooth handler to identify connection lost state
    private final static int CONNECTED = 1;
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();

        }

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Toast.makeText(context, "disconnected", Toast.LENGTH_SHORT).show();

            handler.obtainMessage(CONNECTION_LOST, -1, -1).sendToTarget();
        }

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

            if (btAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                Toast.makeText(context, "BT--OFF", Toast.LENGTH_SHORT).show();
                handler.obtainMessage(CONNECTION_LOST, -1, -1).sendToTarget();
                // The user bluetooth is already disabled.

            }

        }


    }
}
