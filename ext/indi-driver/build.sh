#!/bin/bash
mkdir build
cd build
rm * -rf
cmake -DCMAKE_BUILD_TYPE=Debug -DCMAKE_INSTALL_PREFIX=/usr ../indi-auxremote/
sudo make install
