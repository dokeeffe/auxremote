![](https://raw.githubusercontent.com/dokeeffe/auxremote/master/auxremote.png)

[![Build Status](https://travis-ci.org/dokeeffe/auxremote.svg?branch=master)](https://travis-ci.org/dokeeffe/auxremote) 
[![Coverage Status](https://codecov.io/github/dokeeffe/auxremote/coverage.svg?precision=1)](https://codecov.io/gh/dokeeffe/auxremote)
[![Powered By](https://img.shields.io/badge/powered%20by-springframework-green.svg)](http://projects.spring.io/spring-boot/)
[![Powered By](https://img.shields.io/badge/powered%20by-INDI-green.svg)](http://indilib.org/)

An alternative controller for Celestron telescopes. 

Auxremote is a spring-boot micro service that communicates with a Celestron telescope using the AUX serial commands.
It offers a high level HTTP api to control the mount and enables fully remote operation using the included [ INDI driver](https://github.com/dokeeffe/auxremote/tree/master/ext/indi-driver) .

**Mount** <--> **Handset** <--*serial aux protocol*--> **AUX-Remote** <--*http*--> **INDI_Driver** <--*indi protocol*--> **INDI_Clients**

The following high level features are exposed via the HTTP api

* Park/Unpark
* Sync
* GOTO
* PEC operations
* PulseGuiding

No more star alignment or hibernation mode needed to enable remote use. No more fidilling with the handset by a human, the mount can be aligned remotely from a cold start.

This system is in beta state and is currently being used and tested at [Ballyhoura Observatory](https://twitter.com/ballyhourastars) .
![](http://52-8.xyz/images/allsky.gif)

Special thanks to Andre Paquette for his amazing work [reverse engineering the AUX serial protocol](http://www.paquettefamily.ca/nexstar/NexStar_AUX_Commands_10.pdf) without his work this would not be possible.

## Warning

Use at your own risk. I can provide no guarantee against equipment damage.


## Building / installing

* Build the indi diver. See the README in ext/indi-driver. Use the indi-driver with an indi client such as Kstars/EKOS

* Build the auxremote server side app. See the README in /app

* Connect your mount handset to the machine running the serverside app.