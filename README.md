# auxremote

[![Build Status](https://travis-ci.org/dokeeffe/auxremote.svg?branch=master)](https://travis-ci.org/dokeeffe/auxremote) 
[![Coverage Status](https://codecov.io/github/dokeeffe/auxremote/coverage.svg?precision=1)](https://codecov.io/gh/dokeeffe/auxremote)
[![Powered By](https://img.shields.io/badge/powered%20by-springframework-green.svg)](http://projects.spring.io/spring-boot/)
[![Powered By](https://img.shields.io/badge/powered%20by-INDI-green.svg)](http://indilib.org/)

An alternative controller for Celestron telescopes. 

Auxremote is a spring-boot micro service that communicates with a Celestron telescope using AUX serial commands tunnelled through the handset.
It offers a high level HTTP api to control the mount and enables fully remote operation using the included [ INDI driver](https://github.com/dokeeffe/auxremote/tree/master/ext/indi-driver) .

No more star alignment or hibernation mode needed to enable remote use. The mount can be aligned remotely from a cold start.

THIS IS A WORK IN PROGRESS

This system is in early alpha state and is currently being used and tested at [Ballyhoura Observatory](https://twitter.com/ballyhourastars) .

Special thanks to Andre Paquette for his amazing work [reverse engineering the AUX serial protocol](http://www.paquettefamily.ca/nexstar/NexStar_AUX_Commands_10.pdf)