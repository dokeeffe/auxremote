PROJECT(indi_auxremote CXX C)
cmake_minimum_required(VERSION 2.4.7)

include(GNUInstallDirs)

set(CMAKE_CXX_FLAGS "-std=c++0x ${CMAKE_CXX_FLAGS}")
set(CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake_modules/")

set (VERSION_MAJOR 0)
set (VERSION_MINOR 2)

find_package(INDI REQUIRED)
find_package(Nova REQUIRED)
find_package(ZLIB REQUIRED)

include_directories(${CMAKE_CURRENT_BINARY_DIR})
include_directories(${CMAKE_CURRENT_SOURCE_DIR})
include_directories(${INDI_INCLUDE_DIR})
include_directories(${NOVA_INCLUDE_DIR})
include_directories( ${EV_INCLUDE_DIR})

################ AuxRemote ################
set(auxremote_SRCS
      ${CMAKE_CURRENT_SOURCE_DIR}/auxremotedriver.cpp
      ${CMAKE_CURRENT_SOURCE_DIR}/gason.cpp
   )

add_executable(indi_auxremote ${auxremote_SRCS})
target_link_libraries(indi_auxremote ${INDI_DRIVER_LIBRARIES} ${NOVA_LIBRARIES} curl)
install(TARGETS indi_auxremote RUNTIME DESTINATION bin )
install(FILES indi_auxremote.xml DESTINATION ${INDI_DATA_DIR})
