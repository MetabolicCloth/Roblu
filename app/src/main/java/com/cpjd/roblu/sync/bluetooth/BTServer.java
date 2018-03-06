package com.cpjd.roblu.sync.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import com.cpjd.roblu.io.IO;
import com.cpjd.roblu.models.RCheckout;
import com.cpjd.roblu.models.REvent;
import com.cpjd.roblu.sync.SyncHelper;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;

/**
 * Manages a Bluetooth connection with a client device and data transfer over it.
 *
 * @version 1
 * @since 4.0.0
 * @author Will Davies
 */
public class BTServer extends Thread implements Bluetooth.BluetoothListener {

    /**
     * Provides access to a context reference for accessing the file system
     */
    private Bluetooth bluetooth;

    private SyncHelper syncHelper;

    private REvent event;

    private ProgressDialog pd;

    private ObjectMapper mapper;

    /**
     * Creates a BTServer object for syncing to a Bluetooth device
     */
    public BTServer(ProgressDialog pd, Bluetooth bluetooth) {
        this.pd = pd;
        this.mapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.bluetooth = bluetooth;
        this.bluetooth.setListener(this);
    }

    /**
     * Starts the sync task
     */
    @Override
    public void run() {
        /*
         * Load the active Bluetooth event
         */
        IO io = new IO(bluetooth.getActivity());

        REvent[] events = io.loadEvents();

        if(events == null || events.length == 0) {
            bluetooth.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(bluetooth.getActivity(), "No events found. Please create an event to enable Bluetooth syncing.", Toast.LENGTH_LONG).show();
                }
            });
            pd.dismiss();
            return;
        }

        for(REvent event : events) {
            if(event.isBluetoothEnabled()) {
                this.event = event;
            }
        }

        if(this.event == null) {
            bluetooth.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(bluetooth.getActivity(), "No active Bluetooth event found. Please enable Bluetooth in event settings to enable Bluetooth syncing.", Toast.LENGTH_LONG).show();
                }
            });
            pd.dismiss();
            return;
        }


        this.syncHelper = new SyncHelper(bluetooth.getActivity(), this.event, SyncHelper.MODES.BLUETOOTH);

        if(bluetooth.isEnabled()) {
            bluetooth.startServer();
        } else bluetooth.enable();
    }

    @Override
    public void deviceDiscovered(BluetoothDevice device) {

    }

    @Override
    public void messageReceived(String header, String message) {
        IO io = new IO(bluetooth.getActivity());

        if(header.equals("isActive")) {
            bluetooth.send("ACTIVE", String.valueOf(event == null));
        }
        if(event == null) {
            return;
        }

        switch(header) {
            case "SCOUTING_DATA":
                // Process scouting data
                try {
                    JSONParser parser = new JSONParser();
                    JSONArray array = (JSONArray) parser.parse(message);
                    String[] received = new String[array.size()];
                    for(int i = 0; i < array.size(); i++) received[i] = array.get(i).toString();
                    syncHelper.unpackCheckouts(syncHelper.convertStringSerialToCloudCheckouts(received), null);


                } catch(Exception e) {
                    Log.d("RBS", "Failed to process checkouts received over Bluetooth.");
                }
                break;
            case "requestForm":
                try {
                    bluetooth.send("FORM", mapper.writeValueAsString(io.loadForm(event.getID())));
                } catch(Exception e) {
                    Log.d("RBS", "Failed to parse form as JSON.");
                }
                break;
            case "requestUI":
                try {
                    bluetooth.send("UI", mapper.writeValueAsString(io.loadSettings().getRui()));
                } catch(Exception e) {
                    Log.d("RBS", "Failed to parse form as JSON.");
                }
                break;
            case "requestCheckouts":
                // Get the timestamp
                long time = Long.parseLong(message.split(":")[1]);

                ArrayList<RCheckout> checkouts = syncHelper.generateCheckoutsFromEvent(io.loadTeams(event.getID()), time);

                try {
                    bluetooth.send("CHECKOUTS", mapper.writeValueAsString(syncHelper.packCheckouts(checkouts)));
                } catch(Exception e) {
                    Log.d("RBS", "Failed to map checkouts to Bluetooth output stream.");
                }
                break;
            case "requestNumber":
                bluetooth.send("NUMBER", String.valueOf(io.loadSettings().getTeamNumber()));
                break;
            case "requestEventName":
                bluetooth.send("EVENT_NAME", event.getName());
                break;
            case "DONE":
                bluetooth.send("DONE", "noParams");
                pd.dismiss();
                bluetooth.disconnect();
                if(bluetooth.isEnabled()) {
                    bluetooth.startServer();
                } else bluetooth.enable();
                break;
        }
    }

    @Override
    public void deviceConnected(final BluetoothDevice device) {
        Log.d("RBS", "Connected to device: "+device.getName());

        bluetooth.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pd.setMessage("Connected to device: "+device.getName()+". Syncing...");
            }
        });
    }

    @Override
    public void deviceDisconnected(BluetoothDevice device, String reason) {

    }

    @Override
    public void errorOccurred(String message) {
        Log.d("RBS", "Error occurred: "+message);
    }

    @Override
    public void stateChanged(int state) {
        if(state == BluetoothAdapter.STATE_ON) {
            bluetooth.startServer();
        }
    }

    @Override
    public void discoveryStopped() {

    }
}
