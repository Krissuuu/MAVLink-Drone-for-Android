package com.example.aed_drone;

import android.os.Handler;
import android.util.Log;
import android.widget.Spinner;

import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import io.dronefleet.mavlink.MavlinkConnection;
import io.dronefleet.mavlink.MavlinkMessage;
import io.dronefleet.mavlink.common.CommandLong;
import io.dronefleet.mavlink.common.MavCmd;
import io.dronefleet.mavlink.common.MavFrame;
import io.dronefleet.mavlink.common.MissionItemInt;

public class Drone_Command {

    private static MavlinkConnection connection;
    private static MavlinkMessage message;
    private static Socket socket;
    private static Handler mHandler = MainActivity.mHandler;

    public static void CONNECT(Spinner connect_type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int connect_num = connect_type.getSelectedItemPosition();
                if (connection == null) {
                    try {
                        // TCP connection
                        if (connect_num == 0) {
                            socket = new Socket("192.168.2.230", 5762);
//                            socket = new Socket("192.168.4.1", 6789);
                            connection = MavlinkConnection.create(socket.getInputStream(), socket.getOutputStream());
                        }
                        // USB connection
                        else {
                            connection = MavlinkConnection.create(UsbService.serialInputStream, UsbService.serialOutputStream);
                        }
                        mHandler.obtainMessage(100, "is Connecting...").sendToTarget();
                        // When receive heartbeat message or others
                        while ((message = connection.next()) != null) {
                            Drone_Message.Message_classify(message);
                            mHandler.obtainMessage(100, "Connected...").sendToTarget();
                        }
                    } catch (EOFException eof) {
                        // The stream has ended.
                        Release_Connection();
                        Log.i("Drone_Command", "Connection Failed EOFException (SITL crush)");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        Release_Connection();
                        Log.i("Drone_Command", "Connection Failed UnknownHostException");
                    } catch (IOException e) {
                        e.printStackTrace();
                        Release_Connection();
                        Log.i("Drone_Command", "Connection Failed IOException (MAYBE No WIFI or APP crush)");
                    }
                }
            }
        }).start();
    }
    public static void Release_Connection() {
        try {
            if (socket != null) {
                if (socket.getOutputStream() != null) {
                    socket.getOutputStream().close();
                }
                socket.close();
            }
            connection = null;
            mHandler.obtainMessage(100, "Disconnected...").sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void STATUS() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try {
                        //MAVLINK_MSG_ID_GLOBAL_POSITION_INT 33
                        CommandLong STATUS_Position = new CommandLong.Builder().command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL).param1(33).param2(1000000).param7(0).build();
                        connection.send1(255, 0, STATUS_Position);
                        //MAVLINK_MSG_ID_BATTERY_STATUS 147
                        CommandLong STATUS_Battery = new CommandLong.Builder().command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL).param1(147).param2(1000000).param7(0).build();
                        connection.send1(255, 0, STATUS_Battery);
                        //MAVLINK_MSG_ID_VFR_HUD 74
                        CommandLong STATUS_Speed = new CommandLong.Builder().command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL).param1(74).param2(1000000).param7(0).build();
                        connection.send1(255, 0, STATUS_Speed);
                        //MAVLINK_MSG_ID_SYS_STATUS 1
                        CommandLong STATUS_System = new CommandLong.Builder().command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL).param1(1).param2(1000000).param7(0).build();
                        connection.send1(255, 0, STATUS_System);
                        //MAVLINK_MSG_ID_ATTITUDE 30
                        CommandLong STATUS_Attitude = new CommandLong.Builder().command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL).param1(30).param2(1000000).param7(0).build();
                        connection.send1(255, 0, STATUS_Attitude);
                        //MAVLINK_MSG_ID_GPS_RAW_INT 24
                        CommandLong STATUS_gps = new CommandLong.Builder().command(MavCmd.MAV_CMD_SET_MESSAGE_INTERVAL).param1(24).param2(1000000).param7(0).build();
                        connection.send1(255, 0, STATUS_gps);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void ARM_DISARM_FUNCTION(int is_armed) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try {
                        CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM)
                                .param1(is_armed)
                                .build();
                        connection.send1(255, 0, cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void TAKEOFF_FUNCTION(float alt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try{
                        CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_NAV_TAKEOFF)
                                                                   .param7(alt)
                                                                   .build();
                        connection.send1(255,0, cmd);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void SET_MODE_FUNCTION(int mode_name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try {
                        // param1 = baseMode(MavModeFlag.MAV_MODE_FLAG_CUSTOM_MODE_ENABLED)
                        CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_SET_MODE)
                                                                   .param1(1)
                                                                   .param2(mode_name)
                                                                   .build();
                        connection.send1(255, 0, cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void CHANGE_SPEED_FUNCTION(float speed) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try {
                        CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_CHANGE_SPEED)
                                                                   .param1(0)
                                                                   .param2(speed)
                                                                   .param3(-1)
                                                                   .build();
                        connection.send1(255, 0, cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void CHANGE_YAW_FUNCTION(float angle) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try {
                        CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_CONDITION_YAW)
                                .param1(angle)
                                .param2(1)
                                .param3(-1)
                                .build();
                        connection.send1(255, 0, cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void GOTO_FUNCTION(double lat, double lng, float alt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try {
                        int lat_ = Integer.valueOf((int) (lat * 1e7));
                        int lng_ = Integer.valueOf((int) (lng * 1e7));
                        MissionItemInt mission = new MissionItemInt.Builder().frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT)
                                                                             .command(MavCmd.MAV_CMD_NAV_WAYPOINT)
                                                                             .current(2)
                                                                             .autocontinue(1)
                                                                             .x(lat_).y(lng_).z(alt).build();
                        connection.send1(255, 0, mission);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public static void SERVO_FUNCTION(int pin, int PWM) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection != null) {
                    try {
                        CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_SET_SERVO)
                                                                   .param1(pin)
                                                                   .param2(PWM)
                                                                   .build();
                        connection.send1(255, 0, cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
