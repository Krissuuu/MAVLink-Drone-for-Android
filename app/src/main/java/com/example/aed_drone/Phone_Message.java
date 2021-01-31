package com.example.aed_drone;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static java.lang.Thread.sleep;

public class Phone_Message {

    static String rssi = null, rsrp = null , rsrq = null , rssnr = null, level = null;
    static boolean isConnected = false, isAvailable = false, isFailover = false, isRoaming = false;
    static String device_id = null;
    static boolean quitLooper;
    static ConnectivityManager connectivity;

    public static void Get_Android_ID(Context context){
        try {
            device_id = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.i("Phone_Message", "deviceId "+ device_id);
        }catch (SecurityException e) {
            device_id = null;
            Log.i("Phone_Message", "Get Android ID Failed");
        }
    }
    public static boolean hasSimCard(TelephonyManager telephonyManager) {
        int simState = telephonyManager.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false;
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        Log.i("Phone_Message", result ? "hasSimCard" : "NoSimCard");
        return result;
    }
    public static void ConnectivityManager(ConnectivityManager connectivityManager) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        connectivity = connectivityManager;
                        NetworkInfo mNetworkInfo = connectivityManager.getActiveNetworkInfo();
                        String state_ConnectivityManager;
                        if (mNetworkInfo != null) {
                            isConnected = mNetworkInfo.isConnected();
                            isAvailable = mNetworkInfo.isAvailable();
                            isFailover = mNetworkInfo.isFailover();
                            isRoaming = mNetworkInfo.isRoaming();
                            state_ConnectivityManager = "isConnected: " + isConnected + " isAvailable: " + isAvailable +
                                                        " isFailover: " + isFailover + " isRoaming: " + isRoaming;
                        } else {
                            isConnected = false;
                            isAvailable = false;
                            isFailover = false;
                            isRoaming = false;
                            state_ConnectivityManager = "No Internet!";
                        }
                        Log.i("Phone_Message", state_ConnectivityManager);
                        sleep(1000);
                    } catch (InterruptedException e) {

                    }
                }
            }
        }).start();
    }
    public static boolean PING(){
        try {
            Runtime runtime = Runtime.getRuntime();
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int mExitValue = mIpAddrProcess.waitFor();
            return mExitValue == 0;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public static JSONObject PacketToJson(){

        JSONObject phone_json = new JSONObject();
        JSONObject connectivity = new JSONObject();
//        JSONObject signal_strength = new JSONObject();

        Calendar mCal = Calendar.getInstance();
        CharSequence s = DateFormat.format("yyyy-MM-dd kk:mm:ss", mCal.getTime());    // kk:24小時制, hh:12小時制

        try {
//            signal_strength.put("rssi", rssi);
//            signal_strength.put("rsrp", rsrp);
//            signal_strength.put("rsrq", rsrq);
//            signal_strength.put("rssnr", rssnr);
//            signal_strength.put("level", level);

            connectivity.put("isConnected", isConnected);
            connectivity.put("isAvailable", isAvailable);
            connectivity.put("isFailover", isFailover);
            connectivity.put("isRoaming", isRoaming);

            phone_json.put("timestamp", s);
            phone_json.put("device_id", device_id);
            phone_json.put("connectivity", connectivity);
//            phone_json.put("signal_strength", signal_strength);

        }catch (JSONException e) {
            e.printStackTrace();
        }
        return phone_json;
    }

    public static void Signal_Strength(TelephonyManager telephonyManager) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                quitLooper = false;
                Looper.prepare();
                telephonyManager.listen(new PhoneStateListener() {
                    @Override
                    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                        super.onSignalStrengthsChanged(signalStrength);

                        boolean hasSimCard = hasSimCard(telephonyManager);
                        if(isConnected && hasSimCard){
                            String signal = signalStrength.toString();
                            String signal_LTE = signal.split(",")[4];
                            rssi = signal_LTE.split(" ")[1].replaceAll("[^\\d-.]", "");
                            rsrp = signal_LTE.split(" ")[2].replaceAll("[^\\d-.]", "");
                            rsrq = signal_LTE.split(" ")[3].replaceAll("[^\\d-.]", "");
                            rssnr = signal_LTE.split(" ")[4].replaceAll("[^\\d-.]", "");
                            level = signal_LTE.split(" ")[7].replaceAll("[^\\d-.]", "");
                            Log.i("Phone_Message", "AAA" + signal);
                        }
                        else{
                            rssi = null;
                            rsrp = null;
                            rsrq = null;
                            rssnr = null;
                            level = null;
                            Log.i("Phone_Message", "NO SIM card or NO Internet");
                        }
                        String state_Signal_Strength = "rssi: "+ rssi + " rsrp: " + rsrp + " rsrq: " +
                                rsrq + " rssnr: " + rssnr + " level: " + level;
//                        Log.i("Phone_Message", state_Signal_Strength);

                        if (quitLooper)
                            Looper.myLooper().quit();
                    }
                }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                Looper.loop();
            }
        }).start();
    }
    public void signal_strength_stop() {
        quitLooper = true;
    }
}
