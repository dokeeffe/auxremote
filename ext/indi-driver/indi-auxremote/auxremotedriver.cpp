#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <unistd.h>
#include <sys/time.h>
#include <time.h>
#include <memory>
#include <stdarg.h>

#include <sys/wait.h>
#include <limits.h>

#include "gason.h"
#include <curl/curl.h>
#include <indicom.h>

#include <libnova/sidereal_time.h>
#include <libnova/transform.h>

#include "auxremotedriver.h"

#define	POLLMS      1000
#define PEC_TAB   "PEC"
#define CONNECTION_TAB   "Connection"

std::unique_ptr<AuxRemote> aux_remote(new AuxRemote());

/**
* Callback for curl lib to write response to
**/
static size_t WriteCallback(void *contents, size_t size, size_t nmemb, void *userp) {
    ((std::string*)userp)->append((char*)contents, size * nmemb);
    return size * nmemb;
}

void ISGetProperties(const char *dev) {
  aux_remote->ISGetProperties(dev);
}

void ISNewSwitch(const char *dev, const char *name, ISState *states, char *names[], int num) {
  aux_remote->ISNewSwitch(dev, name, states, names, num);
}

void ISNewText(	const char *dev, const char *name, char *texts[], char *names[], int num) {
  aux_remote->ISNewText(dev, name, texts, names, num);
}

void ISNewNumber(const char *dev, const char *name, double values[], char *names[], int num) {
  aux_remote->ISNewNumber(dev, name, values, names, num);
}

void ISNewBLOB (const char *dev, const char *name, int sizes[], int blobsizes[], char *blobs[], char *formats[], char *names[], int n) {
  INDI_UNUSED(dev);
  INDI_UNUSED(name);
  INDI_UNUSED(sizes);
  INDI_UNUSED(blobsizes);
  INDI_UNUSED(blobs);
  INDI_UNUSED(formats);
  INDI_UNUSED(names);
  INDI_UNUSED(n);
}

void ISSnoopDevice (XMLEle *root) {
  aux_remote->ISSnoopDevice(root);
}

AuxRemote::AuxRemote() {
  SetTelescopeCapability(TELESCOPE_CAN_PARK | TELESCOPE_CAN_GOTO | TELESCOPE_CAN_SYNC | TELESCOPE_CAN_ABORT | TELESCOPE_HAS_TIME | TELESCOPE_HAS_LOCATION, 4);
  currentRA    = 0;
  currentDEC   = 90;
}

AuxRemote::~AuxRemote() {
}

const char * AuxRemote::getDefaultName() {
  return (char *)"Celestron AuxRemote Gateway";
}

bool AuxRemote::initProperties() {
  IUFillText(&CurrentStateMsgT[0],"State","Mount Msgs",NULL);
  IUFillTextVector(&CurrentStateMsgTP, CurrentStateMsgT, 1, getDeviceName(), "STATE", "MOUNT_MSG", MAIN_CONTROL_TAB,IP_RO,60,IPS_IDLE);

  IUFillText(&httpEndpointT[0], "API_ENDPOINT", "API Endpoint", "http://localhost:8080/api");
  IUFillTextVector(&httpEndpointTP, httpEndpointT, 1, getDeviceName(), "HTTP_API_ENDPOINT", "HTTP endpoint", CONNECTION_TAB, IP_RW, 5, IPS_IDLE);
  IUFillText(&auxRemoteSerialDeviceT[0], "AUX_REMOTE_SERIAL_DEVICE", "Remote Server serial dev", "/dev/celestron");
  IUFillTextVector(&auxRemoteSerialDeviceTP, auxRemoteSerialDeviceT, 1, getDeviceName(), "HTTP_AUX_REMOTE_SERIAL_DEVICE", "Remote Server serial dev", CONNECTION_TAB, IP_RW, 5, IPS_IDLE);
  INDI::Telescope::initProperties();

  //pec tab
  IUFillText(&PecT[0], "State", "state", "UNKNOWN");
  IUFillTextVector(&PecTP, PecT, 1, getDeviceName(), "PEC_STATE", "PEC", PEC_TAB, IP_RO, 60, IPS_IDLE);
  IUFillSwitch(&PecModeS[0], "FIND_INDEX", "Find Index", ISS_OFF);
  IUFillSwitch(&PecModeS[1], "RECORD", "Start Recording", ISS_OFF);
  IUFillSwitch(&PecModeS[2], "PLAY", "Start Playback", ISS_OFF);
  IUFillSwitch(&PecModeS[3], "STOP", "Stop Rec/Play", ISS_OFF);
  IUFillSwitchVector(&PecModeSP, PecModeS, 4, getDeviceName(), "PEC_MODE", "Pec Mode", PEC_TAB, IP_RW, ISR_1OFMANY, 0, IPS_IDLE);


  TrackState=SCOPE_IDLE;
  initGuiderProperties(getDeviceName(), MOTION_TAB);
  addDebugControl();
  setDriverInterface(getDriverInterface() | GUIDER_INTERFACE);
  SetParkDataType(PARK_AZ_ALT);
  return true;
}

bool AuxRemote::saveConfigItems(FILE *fp) {
  INDI::Telescope::saveConfigItems(fp);
  IUSaveConfigText(fp, &httpEndpointTP);
  IUSaveConfigText(fp, &auxRemoteSerialDeviceTP);
  return true;
}

void AuxRemote::ISGetProperties(const char *dev) {
  INDI::Telescope::ISGetProperties (dev);
  defineText(&httpEndpointTP);
  defineText(&auxRemoteSerialDeviceTP);
  defineText(&PecTP);
  defineSwitch(&PecModeSP);

}

bool AuxRemote::updateProperties() {
  INDI::Telescope::updateProperties();
  DEBUG(INDI::Logger::DBG_DEBUG, "**updateProperties ");
  if (isConnected())
  {
    defineNumber(&GuideNSNP);
    defineNumber(&GuideWENP);
    defineText(&CurrentStateMsgTP);

    if (InitPark()) {
        // If loading parking data is successful, we just set the default parking values.
        double HA = ln_get_apparent_sidereal_time(ln_get_julian_from_sys());
        double DEC = 90;
        SetAxis1ParkDefault(HA);
        SetAxis2ParkDefault(DEC);
    }
    else
    {
        // Otherwise, we set all parking data to default in case no parking data is found.
        double HA = ln_get_apparent_sidereal_time(ln_get_julian_from_sys());
        double DEC = 90;
        SetAxis1Park(HA);
        SetAxis2Park(DEC);
        SetAxis1ParkDefault(HA);
        SetAxis2ParkDefault(DEC);
    }

  }
  else
  {
      deleteProperty(GuideNSNP.name);
      deleteProperty(GuideWENP.name);
      deleteProperty(CurrentStateMsgTP.name);
  }

  return true;
}

bool AuxRemote::ISNewNumber (const char *dev, const char *name, double values[], char *names[], int n) {
    if(strcmp(dev,getDeviceName())==0) {
        if (!strcmp(name,GuideNSNP.name) || !strcmp(name,GuideWENP.name)) {
            processGuiderProperties(name, values, names, n);
            return true;
        } else if(strcmp(name,"GEOGRAPHIC_COORD")==0) {
            DEBUGF(INDI::Logger::DBG_DEBUG, "**ISNewNumber %s", name);
            int latindex = IUFindIndex("LAT", names, n);
            int longindex= IUFindIndex("LONG", names, n);
            if (latindex == -1 || longindex==-1) {
                DEBUG(INDI::Logger::DBG_ERROR, "Bad location data");
            } else {
              double targetLat  = values[latindex];
              double targetLong = values[longindex];
              //post location to auxremote service
              char _json[60];
              snprintf(_json, 60, "{\"latitude\":%f,\"longitude\":%f}", targetLat, targetLong);
              DEBUGF(INDI::Logger::DBG_DEBUG, "updating loc %s", _json);
              SendPostRequest(_json,"/mount");
            }
         }

      }

    return INDI::Telescope::ISNewNumber(dev, name, values, names, n);
}

bool AuxRemote::ISNewText(const char *dev, const char *name, char *texts[], char *names[], int n) {
  DEBUGF(INDI::Logger::DBG_DEBUG, "**ISNewText %s", name);
  if(!strcmp(dev, getDeviceName())) {
    if (!strcmp(httpEndpointTP.name, name)) {
        IUUpdateText(&httpEndpointTP, texts, names, n);
        httpEndpointTP.s = IPS_OK;
        IDSetText(&httpEndpointTP, NULL);
        return true;
    }
    if (!strcmp(auxRemoteSerialDeviceTP.name, name)) {
        IUUpdateText(&auxRemoteSerialDeviceTP, texts, names, n);
        auxRemoteSerialDeviceTP.s = IPS_OK;
        IDSetText(&auxRemoteSerialDeviceTP, NULL);
        return true;
    }
  }
  return Telescope::ISNewText(dev,name,texts,names,n);
}

bool AuxRemote::ISNewSwitch (const char *dev, const char *name, ISState *states, char *names[], int n) {
  if (!strcmp (getDeviceName(), dev)) {
    if (!strcmp(name, PecModeSP.name)) {
        PecModeSP.s = IPS_IDLE;
        for (int i=0; i < n; i++)
        {
            if (!strcmp(names[i], "FIND_INDEX") && states[i] == ISS_ON) {
              if (TrackState == SCOPE_PARKED) {
                DEBUG(INDI::Logger::DBG_ERROR, "Unpark Mount before using PEC");
              } else {
                SendPostRequest("{\"pecMode\": \"INDEXING\"}","/mount");
              }
            }
            else if (!strcmp(names[i], "RECORD") && states[i] == ISS_ON){
              if (TrackState == SCOPE_PARKED) {
                DEBUG(INDI::Logger::DBG_ERROR, "Unpark Mount before using PEC");
              } else {
                if(pecIndexFound) {
                  SendPostRequest("{\"pecMode\": \"RECORDING\"}","/mount");
                } else {
                  DEBUG(INDI::Logger::DBG_ERROR, "PEC Index not found. Please find index first");
                }
              }
            }
            else if (!strcmp(names[i], "PLAY") && states[i] == ISS_ON) {
              if (TrackState == SCOPE_PARKED) {
                DEBUG(INDI::Logger::DBG_ERROR, "Unpark Mount before using PEC");
              } else {
                if(pecIndexFound) {
                  SendPostRequest("{\"pecMode\": \"PLAYING\"}","/mount");
                } else {
                  DEBUG(INDI::Logger::DBG_ERROR, "PEC Index not found. Please find index first");
                }
              }
            }
            else if (!strcmp(names[i], "STOP") && states[i] == ISS_ON){
                SendPostRequest("{\"pecMode\": \"IDLE\"}","/mount");
            }
        }
        PecModeSP.s = IPS_OK;
    }
  }
  return Telescope::ISNewSwitch(dev,name,states,names,n);
}

bool AuxRemote::Connect() {
  bool connected = false;
  if (httpEndpointT[0].text == NULL)
  {
      DEBUG(INDI::Logger::DBG_ERROR, "HTTP endpoint is not available. Set it in the connection tab");
      return false;
  }
  DEBUGF(INDI::Logger::DBG_DEBUG,  "Updating remote serialPort to %s...", auxRemoteSerialDeviceT[0].text);
  char _json[60];
  //snprintf(_json, 60, "{\"serialPort\":\"%s\"}", "/dev/celestron"); //PortT[0].text); TODO://fix this. changed in panic after indi upgrade
  snprintf(_json, 60, "{\"serialPort\":\"%s\"}", auxRemoteSerialDeviceT[0].text);
  SendPostRequest(_json,"/mount");

  DEBUGF(INDI::Logger::DBG_DEBUG,  "Connecting to %s...", httpEndpointT[0].text);
  connected = SendPostRequest("/mount","/mount/connect");
  if (connected) {
    DEBUG(INDI::Logger::DBG_SESSION, "Succesfully connected");
    ReadScopeStatus();
    SetTimer(POLLMS);
  }
  return connected;
}

bool AuxRemote::Disconnect() {
  DEBUG(INDI::Logger::DBG_ERROR, "Disconnecting");
  return true;
}

bool AuxRemote::ReadScopeStatus() {
  bool result = true;
  //DEBUGF(INDI::Logger::DBG_DEBUG, "Reading status from %s",httpEndpointT[0].text);
  CURL *curl;
  CURLcode res;
  std::string readBuffer;

  curl = curl_easy_init();
  if(curl) {
      char endpoint[50];
      strcpy(endpoint,httpEndpointT[0].text);
      curl_easy_setopt(curl, CURLOPT_URL, strcat(endpoint, "/mount"));
      curl_easy_setopt(curl, CURLOPT_TIMEOUT, 10L); //10 sec timeout
      curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
      curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
      res = curl_easy_perform(curl);
      /* Check for errors */
      if(res != CURLE_OK) {
        DEBUG(INDI::Logger::DBG_ERROR, "API request failed!!!!!!");
        result = false;
        /* always cleanup */
        curl_easy_cleanup(curl);
      } else {
        curl_easy_cleanup(curl);
        //read response
        char srcBuffer[readBuffer.size()];
        strncpy(srcBuffer, readBuffer.c_str(), readBuffer.size());
        char *source = srcBuffer;
        char *endptr;
        JsonValue value;
        JsonAllocator allocator;
        int status = jsonParse(source, &endptr, &value, allocator);
        if (status != JSON_OK) {
            DEBUG(INDI::Logger::DBG_ERROR, "NON OK response from service");
            result = false;
        }
        DEBUGF(INDI::Logger::DBG_DEBUG, "Resp %s", readBuffer.c_str());
        JsonIterator it;
        double ra;
        double dec;
        bool parked;

        for (it = begin(value); it!= end(value); ++it) {
            if (!strcmp(it->key, "raHours")) {
                ra = it->value.toNumber();
            }
            if (!strcmp(it->key, "decDegrees")) {
                dec = it->value.toNumber();
            }
            if (!strcmp(it->key, "pecIndexFound")) {
                pecIndexFound = (it->value.getTag()==JSON_TRUE);
            }
            if (!strcmp(it->key, "pecMode")) {
              char *pecState = it->value.toString();
              PecT[0].text = pecState;
              IDSetText(&PecTP,NULL);
            }
            if (!strcmp(it->key, "statusMessage")) {
              if (it->value.getTag()!=JSON_NULL) {
                IUSaveText(&CurrentStateMsgT[0], it->value.toString());
              } else {
                IUSaveText(&CurrentStateMsgT[0], "");
              }
              IDSetText(&CurrentStateMsgTP, NULL);
            }
            if (!strcmp(it->key, "trackingState") && !isParked()) {
              char *ts = it->value.toString();
              //DEBUGF(INDI::Logger::DBG_DEBUG, "TrackingState= %s", ts);
              if(strcmp(ts,"IDLE")==0) {
                TrackState = SCOPE_IDLE;
              }
              if(strcmp(ts,"TRACKING")==0) {
                TrackState = SCOPE_TRACKING;
                EqNP.s = IPS_OK;
              }
              if(strcmp(ts,"SLEWING")==0) {
                TrackState = SCOPE_SLEWING;
                EqNP.s = IPS_BUSY;
              }
              if(strcmp(ts,"PARKING")==0) {
                DEBUG(INDI::Logger::DBG_SESSION, "status Parking");
                TrackState = SCOPE_PARKING;
                EqNP.s = IPS_BUSY;
              }
              if(strcmp(ts,"PARKED")==0) {
                DEBUG(INDI::Logger::DBG_SESSION, "State Parked");
                if(TrackState == SCOPE_PARKING || TrackState == SCOPE_SLEWING) {
                  SetParked(true);
                  DEBUG(INDI::Logger::DBG_SESSION, "Park succesfull");
                  sleep(5);
                  TrackState = SCOPE_PARKED;
                  EqNP.s = IPS_OK;
                }
              }
            }
            if (!strcmp(it->key, "error") && it->value.getTag()!=JSON_NULL) {
              if(it->value.getTag()==JSON_TRUE) {
                DEBUG(INDI::Logger::DBG_SESSION, "Mount error flag from API is true");
                EqNP.s = IPS_ALERT;
                result = false;
              }

            }
        }
        NewRaDec(ra, dec);
        currentRA = ra;
        currentDEC = dec;
      }
    }
    return result;
}

bool AuxRemote::SendPostRequest(const char *json_payload, const char *path) {
  DEBUGF(INDI::Logger::DBG_DEBUG, "sending request %s to %s", json_payload, path);
  bool status = false;
  CURL *curl;
  CURLcode res;
  curl = curl_easy_init();
  if(curl) {
    struct curl_slist *headers=NULL;
    headers = curl_slist_append(headers, "Content-Type: application/json");
    char endpoint[50];
    strcpy(endpoint,httpEndpointT[0].text);
    curl_easy_setopt(curl, CURLOPT_URL, strcat(endpoint, path));
    curl_easy_setopt(curl, CURLOPT_POSTFIELDS, json_payload);
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    res = curl_easy_perform(curl);
    if(res != CURLE_OK) {
      DEBUG(INDI::Logger::DBG_SESSION, "Request failed");
      status = false;
    } else {
      long http_code = 0;
      curl_easy_getinfo (curl, CURLINFO_RESPONSE_CODE, &http_code);
      if (http_code == 200) {
        status = true;
      } else {
        DEBUGF(INDI::Logger::DBG_ERROR, "Non 200 response from API. Actual response = %d", http_code);
      }
    }
    /* always cleanup */
    curl_easy_cleanup(curl);
  }
  curl_global_cleanup();
  if (status) {
    DEBUGF(INDI::Logger::DBG_SESSION, "%s POST to %s OK", json_payload, path);
  }
  return status;
}

bool AuxRemote::Goto(double ra, double dec) {
  char _json[60];
  snprintf(_json, 60, "{\"raHours\":%f ,\"dec\":%f , \"type\":\"%s\"}", ra, dec, "slew");
  TrackState = SCOPE_SLEWING;
  return SendPostRequest(_json,"/mount/target");
}

bool AuxRemote::Sync(double ra, double dec) {
  char _json[60];
  snprintf(_json, 60, "{\"raHours\":%f ,\"dec\":%f , \"type\":\"%s\"}", ra, dec, "sync");
  EqNP.s    = IPS_OK;
  return SendPostRequest(_json,"/mount/target");
}

/**
 * Convert the stored alt/az park coords to ra/dec and POST a park target request
**/
bool AuxRemote::Park() {
  double parkAZ  = GetAxis1Park();
  double parkAlt = GetAxis2Park();
  char AzStr[16], AltStr[16];
  fs_sexa(AzStr, parkAZ, 2, 3600);
  fs_sexa(AltStr, parkAlt, 2, 3600);
  DEBUGF(INDI::Logger::DBG_SESSION, "Parking to Az (%s) Alt (%s)...", AzStr, AltStr);

  ln_hrz_posn horizontalPos;
  // Libnova south = 0, west = 90, north = 180, east = 270
  horizontalPos.az = parkAZ + 180;
  if (horizontalPos.az >= 360)
      horizontalPos.az -= 360;
  horizontalPos.alt = parkAlt;

  ln_lnlat_posn observer;

  observer.lat = LocationN[LOCATION_LATITUDE].value;
  observer.lng = LocationN[LOCATION_LONGITUDE].value;

  if (observer.lng > 180)
      observer.lng -= 360;

  ln_equ_posn equatorialPos;

  ln_get_equ_from_hrz(&horizontalPos, &observer, ln_get_julian_from_sys(), &equatorialPos);

  char RAStr[16], DEStr[16];
  fs_sexa(RAStr, equatorialPos.ra/15.0, 2, 3600);
  fs_sexa(DEStr, equatorialPos.dec, 2, 3600);
  DEBUGF(INDI::Logger::DBG_SESSION, "Parking to RA (%s) DEC (%s)...", RAStr, DEStr);

  TrackState = SCOPE_PARKING; //TODO: Needed? Maybe the ReadScopeStatus should set this..
  char _json[60];
  snprintf(_json, 60, "{\"raHours\":%f ,\"dec\":%f , \"type\":\"%s\"}", equatorialPos.ra/15.0, equatorialPos.dec, "park");
  bool status = SendPostRequest(_json,"/mount/target");
  if (!status) {
    DEBUG(INDI::Logger::DBG_ERROR, "Failed to park");
    return false;
  }
  DEBUG(INDI::Logger::DBG_ERROR, "Park request sent to mount");
  return true;
}

bool AuxRemote::SetCurrentPark() {
  DEBUG(INDI::Logger::DBG_ERROR, "Setting current park pos");
  ln_hrz_posn horizontalPos;
  // Libnova south = 0, west = 90, north = 180, east = 270

  ln_lnlat_posn observer;
  observer.lat = LocationN[LOCATION_LATITUDE].value;
  observer.lng = LocationN[LOCATION_LONGITUDE].value;
  if (observer.lng > 180)
      observer.lng -= 360;

  ln_equ_posn equatorialPos;
  equatorialPos.ra   = currentRA * 15;
  equatorialPos.dec  = currentDEC;
  ln_get_hrz_from_equ(&equatorialPos, &observer, ln_get_julian_from_sys(), &horizontalPos);

  double parkAZ  = horizontalPos.az - 180;
  if (parkAZ < 0)
      parkAZ += 360;
  double parkAlt = horizontalPos.alt;

  char AzStr[16], AltStr[16];
  fs_sexa(AzStr, parkAZ, 2, 3600);
  fs_sexa(AltStr, parkAlt, 2, 3600);

  DEBUGF(INDI::Logger::DBG_DEBUG, "Setting current parking position to coordinates Az (%s) Alt (%s)...", AzStr, AltStr);

  SetAxis1Park(parkAZ);
  SetAxis2Park(parkAlt);
  return true;
}

bool AuxRemote::SetDefaultPark() {
  // By default set RA to HA
  SetAxis1Park(ln_get_apparent_sidereal_time(ln_get_julian_from_sys()));
  // Set DEC to 90 or -90 depending on the hemisphere
  SetAxis2Park( (LocationN[LOCATION_LATITUDE].value > 0) ? 90 : -90);
  return true;
}


bool AuxRemote::UnPark() {
  double parkAZ  = GetAxis1Park();
  double parkAlt = GetAxis2Park();

  char AzStr[16], AltStr[16];
  fs_sexa(AzStr, parkAZ, 2, 3600);
  fs_sexa(AltStr, parkAlt, 2, 3600);
  DEBUGF(INDI::Logger::DBG_SESSION, "Unparking from Az (%s) Alt (%s)...", AzStr, AltStr);
  ln_hrz_posn horizontalPos;
  // Libnova south = 0, west = 90, north = 180, east = 270
  horizontalPos.az = parkAZ + 180;
  if (horizontalPos.az >= 360)
      horizontalPos.az -= 360;
  horizontalPos.alt = parkAlt;

  ln_lnlat_posn observer;

  observer.lat = LocationN[LOCATION_LATITUDE].value;
  observer.lng = LocationN[LOCATION_LONGITUDE].value;

  if (observer.lng > 180)
      observer.lng -= 360;

  ln_equ_posn equatorialPos;

  ln_get_equ_from_hrz(&horizontalPos, &observer, ln_get_julian_from_sys(), &equatorialPos);

  char RAStr[16], DEStr[16];
  fs_sexa(RAStr, equatorialPos.ra/15.0, 2, 3600);
  fs_sexa(DEStr, equatorialPos.dec, 2, 3600);
  DEBUGF(INDI::Logger::DBG_SESSION, "Unparking and syncing to parked coordinates RA (%s) DEC (%s)...", RAStr, DEStr);
  char _json[60];
  snprintf(_json, 60, "{\"raHours\":%f ,\"dec\":%f , \"type\":\"%s\"}", equatorialPos.ra/15.0, equatorialPos.dec, "unpark");
  bool status = SendPostRequest(_json,"/mount/target");
  if (!status) {
    DEBUG(INDI::Logger::DBG_ERROR, "Failed to unpark");
  } else {
    SetParked(false);
    DEBUG(INDI::Logger::DBG_SESSION, "Unparked");
  }
  return status;
}

bool AuxRemote::MoveNS(INDI_DIR_NS dir, TelescopeMotionCommand command) {
  const char *_move = command == MOTION_STOP ? "abort" : dir == DIRECTION_NORTH ? "north" : "south";
  DEBUGF(INDI::Logger::DBG_ERROR, "Move %s",_move);
  char _rate[] = { (char)('0' + IUFindOnSwitchIndex(&SlewRateSP)), 0 };
  char _json[50];
  snprintf(_json, 50, "{\"type\":\"move\",\"motion\":\"%s\",\"motionRate\":\"%s\"}", _move, _rate);
  bool result =  SendPostRequest(_json, "/mount/target");
  return result;
}

bool AuxRemote::MoveWE(INDI_DIR_WE dir, TelescopeMotionCommand command) {
  const char *_move = command == MOTION_STOP ? "abort" : dir == DIRECTION_WEST ? "west" : "east";
  DEBUGF(INDI::Logger::DBG_ERROR, "Move %s",_move);
  char _rate[] = { (char)('0' + IUFindOnSwitchIndex(&SlewRateSP)), 0 };
  char _json[50];
  snprintf(_json, 50, "{\"type\":\"move\",\"motion\":\"%s\",\"motionRate\":\"%s\"}", _move, _rate);
  return SendPostRequest(_json, "/mount/target");
}

bool AuxRemote::Abort() {
  bool status =  SendPostRequest("{\"type\":\"move\",\"motion\":\"abort\",\"motionRate\":\"0\"}", "/mount/target");
  if (status) {
    DEBUG(INDI::Logger::DBG_SESSION, "Succesfully aborted");
  } else {
    DEBUG(INDI::Logger::DBG_ERROR, "Failed to abort");
  }
  return status;
}

IPState AuxRemote::GuideNorth(float ms) {
  DEBUGF(INDI::Logger::DBG_SESSION, "GUIDE CMD: N %.0f ms", ms);
  char _json[100];
  int _rate = 50;
  snprintf(_json, 100, "{\"type\":\"guide\",\"motion\":\"north\",\"motionRate\":\"%d\", \"guidePulseDurationMs\":\"%.0f\"}", _rate, ms);
  bool status = SendPostRequest(_json, "/mount/target");
  if (status) {
    return IPS_BUSY; //FIXME: should be IPS_OK??:
  } else {
    return IPS_ALERT;
  }
}

IPState AuxRemote::GuideSouth(float ms) {
  DEBUGF(INDI::Logger::DBG_SESSION, "GUIDE CMD: S %.0f ms", ms);
  char _json[100];
  int _rate = 50;
  snprintf(_json, 100, "{\"type\":\"guide\",\"motion\":\"south\",\"motionRate\":\"%d\", \"guidePulseDurationMs\":\"%.0f\"}", _rate, ms);
  bool status = SendPostRequest(_json, "/mount/target");
  if (status) {
    return IPS_BUSY; //FIXME: should be IPS_OK??:
  } else {
    return IPS_ALERT;
  }
}

IPState AuxRemote::GuideEast(float ms) {
  DEBUGF(INDI::Logger::DBG_SESSION, "GUIDE CMD: E %.0f ms", ms);
  char _json[100];
  int _rate = 50;
  snprintf(_json, 100, "{\"type\":\"guide\",\"motion\":\"east\",\"motionRate\":\"%d\", \"guidePulseDurationMs\":\"%.0f\"}", _rate, ms);
  bool status = SendPostRequest(_json, "/mount/target");
  if (status) {
    return IPS_BUSY; //FIXME: should be IPS_OK??:
  } else {
    return IPS_ALERT;
  }
}

IPState AuxRemote::GuideWest(float ms) {
  DEBUGF(INDI::Logger::DBG_SESSION, "GUIDE CMD: W %.0f ms", ms);
  char _json[100];
  int _rate = 50;
  snprintf(_json, 100, "{\"type\":\"guide\",\"motion\":\"west\",\"motionRate\":\"%d\", \"guidePulseDurationMs\":\"%.0f\"}", _rate, ms);
  bool status = SendPostRequest(_json, "/mount/target");
  if (status) {
    return IPS_BUSY; //FIXME: should be IPS_OK??:
  } else {
    return IPS_ALERT;
  }
}
