This directory contains the main application service. 
A spring boot application that communicates with the mount using a serial cable connected to the nexstar handset.

It offers high level functionallity to clients through an HTTP api.
The primary comsumer of this API is the INDI driver although a very basic javascript application is also provided.

### TODO: 
.  Bug fix when used with ekos scheduler then slew never returns (possible driver problem)
.  GOTO and SYNC added to HTML app
.  Implement ALTAZ and EQ-SOUTH tracking. Currently only EQ-NORTH is supported

### API documentation
### TODO
SYNC
GOTO
PARK
UNPARK
MOVE NSEW
GUIDE NSEW
PEC


Installing as a system service on linux

Copy the jar to /opt/auxremote

``
sudo ln -s /opt/auxremote/auxremote-0.0.1-SNAPSHOT.jar /etc/init.d/auxremote
sudo update-rc.d auxremote defaults

``