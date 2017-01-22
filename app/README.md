This directory contains the main application service. 
A spring boot application that communicates with the mount using a serial cable connected to the nexstar handset.

It offers high level functionallity to clients through an HTTP api.
The primary comsumer of this API is the INDI driver although a very basic javascript application is also provided.

### TODO: 
.  Slew limits
.  Scheduled async GPS update of position
.  INDI driver update of position on connect
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