This directory contains the main application service. 
A spring boot application that communicates with the mount using a serial cable connected to the nexstar handset.

It offers high level functionallity to clients through an HTTP api.
The primary comsumer of this API is the INDI driver although a very basic javascript application is also provided.

# Building & Installing

Build the java application with maven

`cd auxremote/app`

`mvn clean install`

The built jar file will be in the target dir. 
To run the app, run with `java -jar auxremote-0.1.2-SNAPSHOT.jar`
 
To verify the app is running, curl to /api/mount

` curl localhost:8080/api/mount`

Should respond with a 500 (since the mount is not connected)

```javascript
{"timestamp":1527945192236,"status":500,"error":"Internal Server Error","exception":"java.lang.IllegalStateException","message":"Not Connected","path":"/api/mount"}
```

Plug your mount handset into a USB port of the machine running this app and use the INDI driver and an indi-client (such as EKOS/kstars) to interact with the mount

### Installing as a system service on linux

Copy the jar to /opt/auxremote

`sudo ln -s /opt/auxremote/auxremote-0.0.1-SNAPSHOT.jar /etc/init.d/auxremote`

`sudo update-rc.d auxremote defaults`

---

## TODO
.  Implement ALTAZ and EQ-SOUTH tracking. Currently only EQ-NORTH is supported

## API documentation

---

**MOUNT STATE**

REQUWATGET /api/mount
RESPONSE

---

**SYNC**

*GET*

/api/mount

---

**GOTO**

*POST*

---

**PARK**

*POST*

---

**UNPARK**

*POST*

---

**MOVE**


*POST*

---

**GUIDE**

*POST*

---




