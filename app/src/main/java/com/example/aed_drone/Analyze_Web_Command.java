package com.example.aed_drone;

import android.util.Log;

import org.json.JSONObject;

public class Analyze_Web_Command {

    public static void Analyze_JSON(String msg_from_mqtt){
        JSONObject json_msg;
        try {
            json_msg = new JSONObject(msg_from_mqtt);
            Object jsonOb = json_msg.get("cmd");
            String web_command = String.valueOf(jsonOb);
            // DRONE_CONTROL
            if(web_command.equals("TAKEOFF")){
                Object takeoff_height = json_msg.get("altitude");
                float height = ObjectToFloat(takeoff_height);
                // Auto change to GUIDED MODE
                Drone_Command.SET_MODE_FUNCTION(4);
                // Auto ARM
                Drone_Command.ARM_DISARM_FUNCTION(1);
                Drone_Command.TAKEOFF_FUNCTION(height);
                Log.i("Analyze_Web_Command", web_command + " "+ height);
            }
            else if (web_command.equals("ARM")){
                Drone_Command.ARM_DISARM_FUNCTION(1);
                Log.i("Analyze_Web_Command", web_command);
            }
            else if (web_command.equals("DISARM")){
                Drone_Command.ARM_DISARM_FUNCTION(0);
                Log.i("Analyze_Web_Command", web_command);
            }
            else if (web_command.equals("GOTO")){
                Object goTo_height = json_msg.get("altitude");
                Object goTo_lat= json_msg.get("lat");
                Object goTo_lng = json_msg.get("lng");
                float altitude = ObjectToFloat(goTo_height);
                double lat = ObjectToFloat(goTo_lat);
                double lng = ObjectToFloat(goTo_lng);
                Drone_Command.GOTO_FUNCTION(lat, lng, altitude);
                Log.i("Analyze_Web_Command", web_command + " " + altitude + " " + lat + " " + lng);
            }
            else if (web_command.equals("CHANGE_SPEED")){
                Object change_speed = json_msg.get("speed");
                float speed = ObjectToFloat(change_speed);
                Drone_Command.CHANGE_SPEED_FUNCTION(speed);
                Log.i("Analyze_Web_Command", web_command + " "+ speed);
            }
            else if (web_command.equals("CHANGE_YAW")){
                Object change_yaw = json_msg.get("angle");
                float angle = ObjectToFloat(change_yaw);
                Drone_Command.CHANGE_YAW_FUNCTION(angle);
                Log.i("Analyze_Web_Command", web_command + " "+ angle);
            }
            // SET_MODE
            else if (web_command.equals("GUIDED")){
                Drone_Command.SET_MODE_FUNCTION(4);
                Log.i("Analyze_Web_Command", web_command);
            }
            else if (web_command.equals("RTL")){
                Drone_Command.SET_MODE_FUNCTION(6);
                Log.i("Analyze_Web_Command", web_command);
            }
            else if (web_command.equals("LAND")){
                Drone_Command.SET_MODE_FUNCTION(9);
                Log.i("Analyze_Web_Command", web_command);
            }
            // SERVO_CONTROL
            else if (web_command.equals("SERVO_UP")){
                Drone_Command.SERVO_FUNCTION(9, 600);
                Log.i("Analyze_Web_Command", web_command);
            }
            else if (web_command.equals("SERVO_DOWN")){
                Drone_Command.SERVO_FUNCTION(9, 2400);
                Log.i("Analyze_Web_Command", web_command);
            }
            else if (web_command.equals("SERVO_STOP")){
                Drone_Command.SERVO_FUNCTION(9, 1500);
                Log.i("Analyze_Web_Command", web_command);
            }
            // GIMBAL
            else if (web_command.equals("GIMBAL_FRONT_BACK")){
                Object GIMBAL_FRONT_BACK_range = json_msg.get("pwm");
                int range = ObjectToInt(GIMBAL_FRONT_BACK_range);
                Drone_Command.SERVO_FUNCTION(10, range);
                Log.i("Analyze_Web_Command", web_command);
            }
            else if (web_command.equals("GIMBAL_LEFT_RIGHT")){
                Object GIMBAL_LEFT_RIGHT_range = json_msg.get("pwm");
                int range = ObjectToInt(GIMBAL_LEFT_RIGHT_range);
                Drone_Command.SERVO_FUNCTION(11, range);
                Log.i("Analyze_Web_Command", web_command);
            }
            else{
                Log.i("Analyze_Web_Command", "Exception WEB Command !!! " + web_command);
            }
        }catch(Exception e){
        }
    }
    public static int ObjectToInt(Object json_obj){
        String s = String.valueOf(json_obj);
        int num = Integer.valueOf(s);
        return num;
    }
    public static float ObjectToFloat(Object json_obj){
        String s = String.valueOf(json_obj);
        float num = Float.valueOf(s);
        return num;
    }
}
