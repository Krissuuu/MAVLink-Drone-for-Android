package com.example.MAVLink_Drone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.content.res.Resources;

import android.net.ConnectivityManager;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.os.Message;

import android.telephony.TelephonyManager;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import java.util.Set;


import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    // USB Connection
    UsbService usbService;
    // Handler
    static MyHandler mHandler;
    // Spinner
    Spinner Spinner_connect_type;
    Spinner Spinner_flight_mode;
    // EditText
    EditText edit_yaw, edit_speed, edit_lat, edit_lng, edit_alt;
    // Upload TextView
    TextView display_PIXHAWK, display_FlightMode, display_isArmed, display_Location, display_Speed, display_Battery;
    TextView display_drone_command;
    TextView display_drone_command_ack;
    // MAVLinkConnection
    MAVLinkConnection MAVLinkConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // KEEP_SCREEN_ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Handle USB message
        mHandler = new MyHandler(this);
        // Spinner variable
        Spinner_connect_type = findViewById(R.id.spinner_connection_type);
        Spinner_flight_mode = findViewById(R.id.spinner_flight_mode);
        // EditText variable
        edit_yaw = findViewById(R.id.edit_yaw);
        edit_speed = findViewById(R.id.edit_speed);
        edit_lat = findViewById(R.id.edit_lat);
        edit_lng = findViewById(R.id.edit_lng);
        edit_alt = findViewById(R.id.edit_alt);
        // TextView variable
        display_drone_command = findViewById(R.id.textView1);
        display_drone_command_ack = findViewById(R.id.textView2);
        display_PIXHAWK = findViewById(R.id.textView_PIXHAWK);
        display_FlightMode = findViewById(R.id.textView_FlightMode);
        display_isArmed = findViewById(R.id.textView_isArmed);
        display_Location = findViewById(R.id.textView_Location);
        display_Speed = findViewById(R.id.textView_Speed);
        display_Battery = findViewById(R.id.textView_Battery);
        // MAVLinkConnection
        MAVLinkConnection = new MAVLinkConnection(mHandler);

        // Flight Mode Spinner
        Resources res = getResources();
        String[] mode = res.getStringArray(R.array.mode);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mode);
        Spinner_flight_mode.setAdapter(adapter);
        Spinner_flight_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = Spinner_flight_mode.getItemAtPosition(i).toString();
                Drone_Command.SET_MODE_FUNCTION(i);
                Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }
    // Drone Control
    public void connect(View view){
        MAVLinkConnection.Create_Connection(Spinner_connect_type);
    }
    public void arm(View view){
        Drone_Command.ARM_DISARM_FUNCTION(1);
    }
    public void disarm(View view){
        Drone_Command.ARM_DISARM_FUNCTION(0);
    }
    public void takeoff(View view){
        float alt = Float.parseFloat("10.5");

        Drone_Command.TAKEOFF_FUNCTION(alt);
    }
    public void change_speed(View view){
        String speed_ = edit_speed.getText().toString().trim();
        float speed = Float.parseFloat(speed_);

        Drone_Command.CHANGE_SPEED_FUNCTION(speed);
    }
    public void change_yaw(View view){
        String angle_ = edit_yaw.getText().toString().trim();
        int angle = Integer.parseInt(angle_);

        Drone_Command.CHANGE_YAW_FUNCTION(angle);
    }
    public void GOTO(View view){
        String lat_ = edit_lat.getText().toString().trim();
        double lat = Double.parseDouble(lat_);
        String lng_ = edit_lng.getText().toString().trim();
        double lng = Double.parseDouble(lng_);
        String alt_ = edit_alt.getText().toString().trim();
        float alt = Float.parseFloat(alt_);

        Drone_Command.GOTO_FUNCTION(lat, lng, alt);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start listening notifications from UsbService
        setFilters();
        // Start UsbService(if it was not started before) and Bind
        startService(UsbService.class, usbConnection, null);
    }
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        MAVLinkConnection.Release_Connection();
    }

    // ****** UsbService ******
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
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
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    // Notifications from UsbService will be received here.
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // This handler will handle TextView UI
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                // USB or TCP connection status (refer to MAVLinkConnection.java)
                case 100:
                    String connection_PIXHAWK = (String) msg.obj;
                    mActivity.get().display_PIXHAWK.setText(connection_PIXHAWK);
                    break;
                // FlightMode
                case 200:
                    String FlightMode = (String) msg.obj;
                    mActivity.get().display_FlightMode.setText(FlightMode);
                    break;
                // isArmed
                case 201:
                    String isArmed = (String) msg.obj;
                    mActivity.get().display_isArmed.setText(isArmed);
                    break;
                // Location
                case 202:
                    String Location = (String) msg.obj;
                    mActivity.get().display_Location.setText(Location);
                    break;
                // Speed
                case 203:
                    String Speed = (String) msg.obj;
                    mActivity.get().display_Speed.setText(Speed);
                    break;
                // Battery
                case 204:
                    String Battery = (String) msg.obj;
                    mActivity.get().display_Battery.setText(Battery);
                    break;
                // CMD
                case 300:
                    String web_cmd = (String) msg.obj;
                    mActivity.get().display_drone_command.append("\n" + web_cmd);
                    break;
                // CMD_ACK
                case 301:
                    String cmd_ack = (String) msg.obj;
                    mActivity.get().display_drone_command_ack.append("\n" + cmd_ack);
                    break;
//                // Sensor Data (refer to UsbService.java)
//                case UsbService.SYNC_READ:
//                    String sensor_data = (String) msg.obj;
//                    break;
            }
        }
    }
}