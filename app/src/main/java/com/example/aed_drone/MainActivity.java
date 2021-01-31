package com.example.aed_drone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.IOException;

import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.annotations.MavlinkMessageInfo;
import io.dronefleet.mavlink.common.AdsbVehicle;
import io.dronefleet.mavlink.common.BatteryStatus;
import io.dronefleet.mavlink.common.CommandAck;
import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.GlobalPositionInt;
import io.dronefleet.mavlink.common.Heartbeat;
import io.dronefleet.mavlink.common.MavCmd;
import io.dronefleet.mavlink.common.MavFrame;
import io.dronefleet.mavlink.common.MavModeFlag;
import io.dronefleet.mavlink.common.MissionItem;
import io.dronefleet.mavlink.common.SetMode;
import io.dronefleet.mavlink.common.SysStatus;
import io.dronefleet.mavlink.common.VfrHud;
import io.dronefleet.mavlink.protocol.MavlinkPacket;
import io.dronefleet.mavlink.serialization.payload.MavlinkPayloadSerializer;
import io.dronefleet.mavlink.serialization.payload.reflection.ReflectionPayloadSerializer;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.filter.hardvideofilter.BaseHardVideoFilter;
import me.lake.librestreaming.filter.hardvideofilter.HardVideoGroupFilter;
import me.lake.librestreaming.ws.StreamAVOption;
import me.lake.librestreaming.ws.StreamLiveCameraView;
import me.lake.librestreaming.ws.filter.hardfilter.GPUImageBeautyFilter;
import me.lake.librestreaming.ws.filter.hardfilter.WatermarkFilter;
import me.lake.librestreaming.ws.filter.hardfilter.extra.GPUImageCompatibleFilter;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    // USB Connection
    private UsbService usbService;
    static MyHandler mHandler;
    // Spinner
    private Spinner Spinner_connect_type;
    private Spinner Spinner_mode;
    // Read Phone State
    TelephonyManager telephonyManager;
    ConnectivityManager connectivityManager;
    // Upload TEXT
    private TextView display_pixhawk;
    private TextView display_mqtt;
    private TextView display1;
    private TextView display2;
    // SeekBar
    private SeekBar mSeekBar_drop;
    private SeekBar mSeekBar_gimbal_FB;
    private SeekBar mSeekBar_gimbal_LR;

//    // Video Streaming
//    private static final String TAG = MainActivity.class.getSimpleName();
//    private StreamLiveCameraView mLiveCameraView;
//    private StreamAVOption streamAVOption;
//    private String rtmpUrl = "rtmp://140.124.71.226/live/test";
//    private LiveUI mLiveUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // KEEP_SCREEN_ON
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // HIDE TITLE
        getSupportActionBar().hide();

        mHandler = new MyHandler(this);
        Spinner_connect_type = findViewById(R.id.spinner);
        Spinner_mode = findViewById(R.id.spinner_mode);
        display_pixhawk = findViewById(R.id.textView_pixhawk);
        display_mqtt = findViewById(R.id.textView_mqtt);

        display1 = findViewById(R.id.textView1);
//        display2 = findViewById(R.id.textView2);

        Phone_Message.Get_Android_ID(this.getApplicationContext());

//        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        Phone_Message.Signal_Strength(telephonyManager);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Phone_Message.ConnectivityManager(connectivityManager);

        // Flight Mode Spinner
        Resources res = getResources();
        String[] mode = res.getStringArray(R.array.mode);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mode);
        Spinner_mode.setAdapter(adapter);
        Spinner_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String text = Spinner_mode.getItemAtPosition(i).toString();
                Drone_Command.SET_MODE_FUNCTION(i);
                Toast.makeText(MainActivity.this,text,Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Seek_Bar(Drop/GIMBAL)
        mSeekBar_drop = (SeekBar) findViewById(R.id.seekBar_drop);
        mSeekBar_drop.setOnSeekBarChangeListener(seekBarOnSeekBarChange_drop);
        mSeekBar_gimbal_FB = (SeekBar) findViewById(R.id.seekBar_gimbal_FB);
        mSeekBar_gimbal_FB.setOnSeekBarChangeListener(seekBarOnSeekBarChange_gimbal_FB);
        mSeekBar_gimbal_LR = (SeekBar) findViewById(R.id.seekBar_gimbal_LR);
        mSeekBar_gimbal_LR.setOnSeekBarChangeListener(seekBarOnSeekBarChange_gimbal_LR);

        UPDATE_DRONE_INFO();
//        // Video Streaming
//        initLiveConfig();
//        mLiveUI = new LiveUI(this,mLiveCameraView,rtmpUrl);

//        // set libwslive
//        request_permissions();
//        StatusBarUtils.setTranslucentStatus(this);
//        initLiveConfig();
    }
    // SERVO CONTROL
    private SeekBar.OnSeekBarChangeListener seekBarOnSeekBarChange_drop = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            int seekProgress = mSeekBar_drop.getProgress();
            int value = 1500;
            switch (seekProgress){
                case 0:
                    value = 600;
                    break;
                case 1:
                    value = 900;
                    break;
                case 2:
                    value = 1200;
                    break;
                case 3:
                    value = 1500;
                    break;
                case 4:
                    value = 1800;
                    break;
                case 5:
                    value = 2100;
                    break;
                case 6:
                    value = 2400;
                    break;
            }
            Drone_Command.SERVO_FUNCTION(9, value);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
    };
    // GIMBAL_FB CONTROL
    private SeekBar.OnSeekBarChangeListener seekBarOnSeekBarChange_gimbal_FB = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            int seekProgress = mSeekBar_gimbal_FB.getProgress();
            int value = 1500;
            switch (seekProgress){
                case 0:
                    value = 1200;
                    break;
                case 1:
                    value = 1300;
                    break;
                case 2:
                    value = 1400;
                    break;
                case 3:
                    value = 1500;
                    break;
                case 4:
                    value = 1600;
                    break;
                case 5:
                    value = 1700;
                    break;
                case 6:
                    value = 1800;
                    break;
            }
            Drone_Command.SERVO_FUNCTION(10, value);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
    };
    // GIMBAL_LR CONTROL
    private SeekBar.OnSeekBarChangeListener seekBarOnSeekBarChange_gimbal_LR = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            int seekProgress = mSeekBar_gimbal_LR.getProgress();
            int value = 1500;
            switch (seekProgress){
                case 0:
                    value = 1200;
                    break;
                case 1:
                    value = 1300;
                    break;
                case 2:
                    value = 1400;
                    break;
                case 3:
                    value = 1500;
                    break;
                case 4:
                    value = 1600;
                    break;
                case 5:
                    value = 1700;
                    break;
                case 6:
                    value = 1800;
                    break;
            }
            Drone_Command.SERVO_FUNCTION(11, value);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }
    };
    // DRONE CONTROL
    public void connect(View view){
        Drone_Command.CONNECT(Spinner_connect_type);
    }
    public void arm(View view){
        Drone_Command.ARM_DISARM_FUNCTION(1);
    }
    public void disarm(View view){
        Drone_Command.ARM_DISARM_FUNCTION(0);
    }
    public void mqtt_connect(View view){
        MQTT.MQTT_connection(this.getApplicationContext());
    }
    public void takeoff(View view){
        Float alt = new Float(10.5);
        Drone_Command.TAKEOFF_FUNCTION(alt);
    }
    public void status(View view){
        Drone_Command.STATUS();
    }

//    // Video Streaming
//    public void initLiveConfig() {
//        mLiveCameraView = (StreamLiveCameraView) findViewById(R.id.stream_previewView);
//
//        //参数配置 start
//        streamAVOption = new StreamAVOption();
//        streamAVOption.streamUrl = rtmpUrl;
//        //参数配置 end
//
//        mLiveCameraView.init(this, streamAVOption);
//        mLiveCameraView.addStreamStateListener(resConnectionListener);
//        LinkedList<BaseHardVideoFilter> files = new LinkedList<>();
//        files.add(new GPUImageCompatibleFilter(new GPUImageBeautyFilter()));
//        mLiveCameraView.setHardVideoFilter(new HardVideoGroupFilter(files));
//    }
//    RESConnectionListener resConnectionListener = new RESConnectionListener() {
//        @Override
//        public void onOpenConnectionResult(int result) {
//            // 0 : Success 1 : Fail
//            Toast.makeText(MainActivity.this,"Status: ： "+result+ " url："+rtmpUrl,Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        public void onWriteError(int errno) {
//            Toast.makeText(MainActivity.this,"Failed",Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        public void onCloseConnectionResult(int result) {
//            //result 0成功  1 失败
//            Toast.makeText(MainActivity.this,"Close: ："+result,Toast.LENGTH_LONG).show();
//        }
//    };
//    private void request_permissions() {
//        // 创建一个权限列表，把需要使用而没用授权的的权限存放在这里
//        List<String> permissionList = new ArrayList<>();
//
//        // 判断权限是否已经授予，没有就把该权限添加到列表中
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.CAMERA);
//        }
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.RECORD_AUDIO);
//        }
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        }
//
//        // 如果列表为空，就是全部权限都获取了，不用再次获取了。不为空就去申请权限
//        if (!permissionList.isEmpty()) {
//            ActivityCompat.requestPermissions(this,
//                    permissionList.toArray(new String[permissionList.size()]), 1002);
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind
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
        Log.i("SocketService", "onDestroy");
        Drone_Command.Release_Connection();
//        // Video Streaming
//        mLiveCameraView.destroy();
    }
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
                    mHandler.obtainMessage(100, "Disconnected...").sendToTarget();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // This handler will be passed to UsbService. Data received from serial port is displayed through this handler
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
//                    mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
//                    mActivity.get().display.append(buffer);
                    break;
                // USB or TCP connection status
                case 100:
                    String connection_pixhawk = (String) msg.obj;
                    mActivity.get().display_pixhawk.setText(connection_pixhawk);
                    break;
                // MQTT connection status
                case 101:
                    String connection_mqtt = (String) msg.obj;
                    mActivity.get().display_mqtt.setText(connection_mqtt);
                    break;
                //  DRONE INFO
                case 110:
                    String cmd = (String) msg.obj;
                    mActivity.get().display1.setText(cmd);
                    break;
//                case 111:
//                    String ack = (String) msg.obj;
//                    mActivity.get().display2.setText(ack);
//                    break;
            }
        }
    }
    public static void UPDATE_DRONE_INFO() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        JSONObject Drone_message_json = Drone_Message.PacketToJson();

                        String timestamp = Drone_message_json.getString("timestamp");

                        JSONObject location = Drone_message_json.getJSONObject("location");
                        String location_str = location.toString();
                        JSONObject battery = Drone_message_json.getJSONObject("battery");
                        String battery_str = battery.toString();
                        JSONObject speed = Drone_message_json.getJSONObject("speed");
                        String speed_str = speed.toString();
                        JSONObject attitude = Drone_message_json.getJSONObject("attitude");
                        String attitude_str = attitude.toString();
                        JSONObject gps_status = Drone_message_json.getJSONObject("gps_status");
                        String gps_status_str = gps_status.toString();
                        JSONObject heartbeat = Drone_message_json.getJSONObject("heartbeat");
                        String heartbeat_str = heartbeat.toString();

                        String drone_info = "timestamp : " + timestamp + "\n\n" +
                                            "location : " + location_str + "\n\n" +
                                            "battery : " + battery_str + "\n\n" +
                                            "speed : " + speed_str + "\n\n" +
                                            "attitude : " + attitude_str + "\n\n" +
                                            "gps_status : " + gps_status_str + "\n\n" +
                                            "heartbeat : " + heartbeat_str;

                        mHandler.obtainMessage(110, drone_info).sendToTarget();
                        sleep(1000);
                    } catch (InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

//    public void TEST() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try{
//                    SetMode mode = new SetMode.Builder().baseMode(MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED).customMode(4).build();
//                    connection.send1(255,0, mode);
//
//                    MavlinkPayloadSerializer serializer = new ReflectionPayloadSerializer();
//                    MavlinkMessageInfo messageInfo = mode.getClass().getAnnotation(MavlinkMessageInfo.class);
//                    byte[] serializedPayload = serializer.serialize(mode);
//                    int sequence = 0;
//                    MavlinkPacket packet = MavlinkPacket.createMavlink1Packet(
//                            sequence,
//                            255,
//                            0,
//                            messageInfo.id(),
//                            messageInfo.crc(),
//                            serializedPayload);
//
//                    Log.i("COMMAND", "content: " + packet.toString());
//                    Log.i("COMMAND", "content: " + packet.getPayload());
//                    Log.i("COMMAND", "content: " + packet.getRawBytes());
//
//                    MavlinkPacket packet_back = packet.fromV1Bytes(packet.getRawBytes());
//                    Log.i("COMMAND", "content: " + packet_back.toString());
//                    Log.i("COMMAND", "content: " + packet_back.getPayload());
//                    Log.i("COMMAND", "content: " + packet_back.getRawBytes());
//
//                    byte[] bytes = new byte[5];
//                    bytes[0] = (byte)0x81;
//                    Log.i("COMMAND", "content: " + bytes[0]);
//
//                }catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
}