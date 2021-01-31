package com.example.aed_drone;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

public class MQTT{

    private static MqttAndroidClient client;
    static boolean mqtt_isconnected = false;
    static boolean publish_stop = true;
    private static Handler mHandler = MainActivity.mHandler;

    public static void MQTT_connection(Context context){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(Phone_Message.PING() && (mqtt_isconnected == false)){
                    String clientId = MqttClient.generateClientId();
                    String MQTT_HOST = "tcp://35.201.182.150:1883";
                    client = new MqttAndroidClient(context, MQTT_HOST, clientId);
                    try {
                        // CONNECT TO MQTT_BROKER
                        IMqttToken token = client.connect();
                        token.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                mqtt_isconnected = true;
                                if(publish_stop){
                                    MQTT_PUBLISH_MESSAGE();
                                }
                                MQTT_SUBSCRIBE();
                                mHandler.obtainMessage(101, "Connected...").sendToTarget();
                                Log.i("MQTT",  "connection Successful");
                            }
                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                mqtt_isconnected = false;
                                mHandler.obtainMessage(101, "Disconnected...").sendToTarget();
                                MQTT_AUTO_RECONNECT(context);
                                Log.i("MQTT",  "connection Failure");
                            }
                        });
                        // RECEIVE MQTT MESSAGE
                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {
                                mqtt_isconnected = false;
                                mHandler.obtainMessage(101, "Disconnected...").sendToTarget();
                                MQTT_AUTO_RECONNECT(context);
                                Log.i("MQTT",  "connectionLost");
                            }
                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                Analyze_Web_Command.Analyze_JSON(new String(message.getPayload()));
                                Log.i("MQTT",  "TOPIC_NAME is " + topic + " PAYLOAD is " + new String(message.getPayload()));
                            }
                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Log.i("MQTT",  "No internet or MQTT has Connected!");
                }
            }
        }).start();
    }
    public static void MQTT_disconnection(){
        try {
            if(mqtt_isconnected == true && publish_stop == true) {
                IMqttToken disconToken = client.disconnect();
                disconToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i("MQTT", "MQTT disconnect Successful");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.i("MQTT", "MQTT disconnect Failure");
                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    public static void MQTT_AUTO_RECONNECT(Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mqtt_isconnected == false) {
                    try {
                        MQTT_connection(context);
                        Log.i("MQTT", "AUTO RECONNECT...");
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void MQTT_SUBSCRIBE(){
        try {
            if(mqtt_isconnected == true) {
                int qos = 1;
                IMqttToken subToken = client.subscribe("drone/cmd", qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.i("MQTT", "MQTT SUBSCRIBE drone/cmd Successful");
                    }
                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        MQTT_SUBSCRIBE();
                        Log.i("MQTT", "MQTT SUBSCRIBE drone/cmd Failure");
                    }
                });
            }
        }catch (MqttException e){
            e.printStackTrace();
        }
    }
    public static void MQTT_PUBLISH_MESSAGE() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mqtt_isconnected){
                    try {
                        publish_stop = false;
                        JSONObject json = new JSONObject();
                        JSONObject Drone_message_json = Drone_Message.PacketToJson();
                        JSONObject Phone_message_json = Phone_Message.PacketToJson();
                        json.put("Drone", Drone_message_json);
                        json.put("Phone", Phone_message_json);
                        client.publish("drone/message", json.toString().getBytes(), 0, false);
                        sleep(1000);
                    } catch (MqttException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                if(!mqtt_isconnected){
                    publish_stop = true;
                }
            }
        }).start();
    }
    public static void MQTT_PUBLISH_CMD_ACK(String cmd_ack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mqtt_isconnected){
                    try {
                        client.publish("drone/cmd_ack", cmd_ack.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void MQTT_PUBLISH_MISSION_ACK(String mission_ack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mqtt_isconnected){
                    try {
                        client.publish("drone/mission_ack", mission_ack.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void MQTT_PUBLISH_APM_TEXT(String apm_text) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mqtt_isconnected){
                    try {
                        client.publish("drone/apm_text", apm_text.getBytes(), 0, false);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
