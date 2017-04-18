#!/bin/bash
dbus-send --session --print-reply --dest="org.kde.kstars" /KStars/Ekos/Dome org.kde.kstars.Ekos.Dome.isMoving
dbus-send --session --print-reply --dest="org.kde.kstars" /KStars/Ekos/Mount org.kde.kstars.Ekos.Mount.getSlewStatus
