# MAVLink Drone for Android

- [Build ArduPilot SITL](#A)
- [How to use](#B)
- [MAVLink Command Usage](#C)
- [Reference](#D)


## *<a id="A">Build ArduPilot SITL</a>*
1. ***If you use Linux pls skip this step***, Install WSL (Win10 Subsystem).
2. Follow https://ardupilot.org/dev/docs/building-setup-linux.html#building-setup-linux to set up the SITL environment.
3. Follow https://github.com/ArduPilot/ardupilot/blob/master/BUILD.md to build the SITL source code. ***I use ./waf configure --board sitl***
4. ***If you use Linux pls skip this step***, Download https://sourceforge.net/projects/vcxsrv/ to visualize the Linux windows.
5. Input the command ***<sim_vehicle.py -v ArduCopter --console>*** on WSL terminal to run SITL. ***Input command <sim_vehicle.py -h> to see how to use the exist parameters.***
6. Open ***Mission Planner*** or ***QGroundControl*** to control and monitor the virtual drone.


## *<a id="B">How to use</a>*

1. Select ***"USB"*** option and ***connect to Pixhawk*** via USB or select ***"TCP"*** and ***connect to SITL*** via networks. ***If successful, "Pixhawk Status"*** will be displayed ***"Connected..."***.
2. You can change ***flight mode (STABILIZE / ACRO / AUTO / GUIDED...)*** using the drop-down menu beside the "CONNECT" button.
3. You can make drone ***ARM / DISARM / TAKEOFF*** using the corresponding button.
4. You can ***change the drone's flight speed*** using the ***"CHANGE SPEED" button*** after you input the flight speed you want.
5. You can ***change the drone's yaw angle*** using the ***"CHANGE YAW" button*** after you input the yaw angle you want.
6. You can let the drone ***fly to the destination*** you want, you have to input ***latitude / longitude / altitude*** and the press the ***"GOTO" button***.

**★Please change the flght mode to "GUIDED" before you control the drone.**
### Reference
★ If you want to modify the IP & port of ArduPilot SITL. You can change the code from ```MAVLinkConnection.java```.
```java=0
socket = new Socket("192.168.2.118", 5762);
```

## *<a id="C">MAVLink Command Usage</a>*
### :memo: Control Drone

### ★ Arm & Disarm
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_COMPONENT_ARM_DISARM (400)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM).param1(is_armed).param2(0).build();
connection.send1(255,0, cmd);

| is_armed | Status |
| ---------| -------|
| 0        | DISARM |
| 1        | ARM    |
```
### ★ TakeOff
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_NAV_TAKEOFF (22)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_NAV_TAKEOFF).param1(15).param2(0).param3(0).param4(0).param5(0).param6(0).param7(takeoff_alt).build();
connection.send1(255,0, cmd);
```
### ★ Set Flight Mode
#### Bulid a **COMMAND_LONG (#76)** Message and use **MavCmd.MAV_CMD_DO_SET_MODE (176)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(CustomMode).build();
connection.send1(255, 0, cmd);

| CustomMode | FlightMode |
| ---------- | -----------|
| 0          | STABILIZE  |
| 1          | ACRO       |
| 2          | ALT_HOLD   |
| 3          | AUTO       |
| 4          | GUIDED     |
| 5          | LOITER     |
| 6          | RTL        |
| 7          | CIRCLE     |
| 9          | LAND       |
```
### ★ Change Flight Speed
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_DO_CHANGE_SPEED (178)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_CHANGE_SPEED).param1(0).param2(speed).param3(-1).param4(0).build();
connection.send1(255,0, cmd);
```

### ★ Change Flight Yaw
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_CONDITION_YAW (115)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_CONDITION_YAW).param1(angle).param2(1).param3(-1).build();
connection.send1(255, 0, cmd);
```

### ★ Control Servo
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_DO_SET_SERVO (183)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_SET_SERVO).param1(pin).param2(PWM).build();
connection.send1(255,0, cmd);
```

### ★ Go to
#### Bulid a **MISSION_ITEM (#39)** Message and use **MAV_CMD_NAV_WAYPOINT (16)** command.
```java=0
MissionItem mission = new MissionItem.Builder().command(MavCmd.MAV_CMD_NAV_WAYPOINT).targetSystem(0).targetComponent(0).seq(0).current(2).autocontinue(0).frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT).x(lat).y(lng).z(alt).build();
connection.send1(255,0, mission);
```

### :memo: Receive  Flight Message
★ Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_SET_MESSAGE_INTERVAL (511)** command to listen the message you want and set the transmission interval.
```java=0
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
```
| msg                 | msg_id | Link                 |
| ------------------- | ------ | -------------------- |
| HEARTBEAT           | 0      | [:link:][HEARTBEAT]  |
| GLOBAL_POSITION_INT | 33     | [:link:][POSITION]   |
| VFR_HUD             | 74     | [:link:][VFR_HUD]    |
| SYS_STATUS          | 1      | [:link:][SYS_STATUS] | 
| ATTITUDE            | 30     | [:link:][ATTITUDE]   |
| GPS_RAW_INT         | 24     | [:link:][GPS_RAW_INT]|

[HEARTBEAT]: https://mavlink.io/en/messages/common.html#HEARTBEAT
[POSITION]: https://mavlink.io/en/messages/common.html#GLOBAL_POSITION_INT
[VFR_HUD]: https://mavlink.io/en/messages/common.html#VFR_HUD
[SYS_STATUS]: https://mavlink.io/en/messages/common.html#SYS_STATUS
[ATTITUDE]: https://mavlink.io/en/messages/common.html#ATTITUDE
[GPS_RAW_INT]: https://mavlink.io/en/messages/common.html#GPS_RAW_INT

★ You can receive and handle drone's message from ```Drone_Message.java```. Take ***GLOBAL_POSITION_INT ( #33 )*** as an example.

```java=0
// GLOBAL_POSITION_INT ( #33 )
else if(message.getPayload() instanceof GlobalPositionInt){
    MavlinkMessage<GlobalPositionInt> positionMessage = (MavlinkMessage<GlobalPositionInt>)message;
    String payload = "" + positionMessage.getPayload();
    String[] payload_GlobalPositionInt = payload.replaceAll("[^\\d-.,E]", "").split(",");
    String lat_ = payload_GlobalPositionInt[1];
    String lng_ = payload_GlobalPositionInt[2];
    String alt_ = payload_GlobalPositionInt[3];
    String relative_alt_ = payload_GlobalPositionInt[4];
    String heading_ = payload_GlobalPositionInt[8];
    try{
        lat = String.valueOf(Double.parseDouble(lat_) / 10000000);
        lng = String.valueOf(Double.parseDouble(lng_) / 10000000);
        alt = String.valueOf(Double.parseDouble(alt_) / 1000);
        relative_alt = String.valueOf(Double.parseDouble(relative_alt_) / 1000);
        heading = String.valueOf(Double.parseDouble(heading_) / 100);
    }catch(NumberFormatException e){
        e.printStackTrace();
    }
    Log.i("Drone_Message", "GLOBAL_POSITION_INT: " + lat + " " + lng + " " + alt + " " + relative_alt + " " + heading);
}
```

## *<a id="D">Reference</a>*

1. https://github.com/dronefleet/mavlink
2. https://github.com/felHR85/UsbSerial

