# How to use

## Step1
You can choose to use Wi-Fi or USB to connect to Pixhawk, but if you use Wi-Fi, you need to add a WIFI module and connect to the Telem1 port of Pixhawk. 

After you select the connection method, click "CONNECT" and if the connection is successful, "Pixhawk Status" will be displayed as "Connected".

If you use Wi-Fi, you need to change the Socket path, please refer to the figure below.

![](https://i.imgur.com/5cSE3v8.png)

## Step2
The "MQTT CONNECT" button is used to connect to the MQTT Broker. 

If the connection is successful, "MQTT Status" will be displayed as "Connected". 

If you need to change the Broker path, please refer to the figure below.

![](https://i.imgur.com/NcXEqMW.png)

## Step3
The "DRONE STATUS" button is used to notify Pixhawk and let it return the current drone information. 

After success, various information will appear below, refer to the figure below.

![](https://i.imgur.com/f8QAeYS.png)

## Step4
The drop-down menu on the left of "CONNECT" is used to set the flight mode.

"ARM" "DISARM" "TAKEOFF" button is used to control ARM, DISARM, TAKEOFF.

The "Drop AED" button is used to control the servo motor to drop the AED.

The "Gimbal_Front_Back" button is used to control the forward and backward tilt of the gimbal.

The "Gimbal_Left_Right" button is used to control the left and right tilt of the gimbal.

![](https://i.imgur.com/V4WsMR7.png)



# Mavlink Command Usage

## :memo: Control Drone

### :pushpin:Arm & Disarm
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_COMPONENT_ARM_DISARM (400)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_COMPONENT_ARM_DISARM).param1(is_armed).param2(0).build();
connection.send1(255,0, cmd);
```
### :pushpin:Takeoff
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_NAV_TAKEOFF (22)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_NAV_TAKEOFF).param1(15).param2(0).param3(0).param4(0).param5(0).param6(0).param7(takeoff_alt).build();
connection.send1(255,0, cmd);
```
### :pushpin:SetMode
#### Bulid a **COMMAND_LONG (#76)** Message and use **MavCmd.MAV_CMD_DO_SET_MODE (176)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_SET_MODE).param1(1).param2(CustomMode).build();
connection.send1(255, 0, cmd);
```
| CustomMode | Flight Mode    |
| ---------- | -------------- |
| 0          | STABILIZE      |
| 1          | ACRO           |
| 2          | ALT_HOLD       |
| 3          | AUTO           |
| 4          | GUIDED         |
| 5          | LOITER         |
| 6          | RTL            |
| 7          | CIRCLE         |
| 9          | LAND           |

### :pushpin:Change_Speed
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_DO_CHANGE_SPEED (178)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_CHANGE_SPEED).param1(0).param2(speed).param3(-1).param4(0).build();
connection.send1(255,0, cmd);
```

### :pushpin:Change_Yaw
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_CONDITION_YAW (115)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_CONDITION_YAW).param1(angle).param2(1).param3(-1).build();
connection.send1(255, 0, cmd);
```

### :pushpin:Servo_Control
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_DO_SET_SERVO (183)** command.
```java=0
CommandLong cmd = new CommandLong.Builder().command(MavCmd.MAV_CMD_DO_SET_SERVO).param1(pin).param2(PWM).build();
connection.send1(255,0, cmd);
```

### :pushpin:GoTo
#### Bulid a **MISSION_ITEM (#39)** Message and use **MAV_CMD_NAV_WAYPOINT (16)** command.
```java=0
MissionItem mission = new MissionItem.Builder().command(MavCmd.MAV_CMD_NAV_WAYPOINT).targetSystem(0).targetComponent(0).seq(0).current(2).autocontinue(0).frame(MavFrame.MAV_FRAME_GLOBAL_RELATIVE_ALT).x(lat).y(lng).z(alt).build();
connection.send1(255,0, mission);
```

## :memo: Receive  Flight Message
#### Bulid a **COMMAND_LONG (#76)** Message and use **MAV_CMD_SET_MESSAGE_INTERVAL (511)** command to listen the message you want and set the transmission interval.
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

### :pushpin:Custom Drone Message

```json=0
{
  "Drone": {
    "timestamp": "2020-12-23 22:43:01",
    "location": {
      "lat": "25.0430116",
      "lng": "121.536214",
      "alt": "13.04",
      "relative_alt": "3.003",
      "heading": "95.42"
    },
    "battery": {
      "voltage": "12.587",
      "current": "28.16",
      "percentage": "0"
    },
    "speed": {
      "air_speed": "0.01",
      "gnd_speed": "0.10"
    },
    "attitude": {
      "roll": "-0.38",
      "pitch": "-0.40",
      "yaw": "95.42"
    },
    "gps_status": {
      "fix_type": "GPS_FIX_TYPE_RTK_FIXED",
      "hpop": "1.21",
      "vdop": "2.00",
      "cog": "22054",
      "gps_count": "10"
    },
    "heartbeat": {
      "mav_type": "MAV_TYPE_QUADROTOR",
      "mav_autopilot": "MAV_AUTOPILOT_ARDUPILOTMEGA",
      "flight_mode": "GUIDED",
      "system_status": "MAV_STATE_ACTIVE",
      "is_armed": "1"
    }
  }
}
```

#### :memo:**location** Message from **GLOBAL_POSITION_INT (#33)** Message

| location     | description                     | 
| ------------ | -----------                     | 
| lat          | Latitude                        | 
| lng          | Longitude                       |
| alt          | Altitude (MSL)                  |
| relative_alt | Altitude above ground           |
| heading      | Vehicle heading (0~360 degrees) |

#### :memo:**battery** Message from **SYS_STATUS (#1)** Message

| battery      | description              | 
| ------------ | -----------              | 
| voltage      | Battery voltage          | 
| current      | Battery current          |
| percentage   | Battery energy remaining |


#### :memo:**speed** Message from **VFR_HUD (#74)** Message

| speed        | description             | 
| ------------ | -----------             | 
| airspeed     | Current air speed       | 
| groundspeed  | Current ground speed    |

#### :memo:**attitude** Message from **ATTITUDE (#30)** Message

| attitude     | description            | 
| ------------ | -----------            | 
| row     | Roll angle (-pi..+pi)       | 
| pitch   | Pitch angle (-pi..+pi)      |
| yaw     | Yaw angle (-pi..+pi)        |

#### :memo:**gps_status** Message from **GPS_RAW_INT (#24)** Message

| gps_status | description                              | 
| ---------- | -----------                              | 
| fix_type   | GPS fix type                             | 
| hpop       | GPS HDOP horizontal dilution of position |
| vdop       | GPS VDOP vertical dilution of position   |
| cog        | Course over ground (0.0..359.99 degrees) |
| gps_count  | Number of satellites visible             |

#### :memo:**heartbeat** Message from **HEARTBEAT (#0)** Message

| heartbeat     | description                                        | 
| ------------  | -----------                                        | 
| mav_type      | Vehicle or component type                          | 
| mav_autopilot | Autopilot type / class                             |
| flight_mode   | Current Flight mode                                |
| system_status | System status flag                                 |
| is_armed      | Whether the drone is armed (0 : Disarmed 1 : Armed)|
