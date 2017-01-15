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

#include "auxremotedriver.h"

#define	POLLMS      1000

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
  SetTelescopeCapability(TELESCOPE_CAN_PARK | TELESCOPE_CAN_SYNC | TELESCOPE_CAN_ABORT | TELESCOPE_HAS_TIME | TELESCOPE_HAS_LOCATION, 4);
  currentRA    = 0;
  currentDEC   = 90;
}

AuxRemote::~AuxRemote() {
}

const char * AuxRemote::getDefaultName() {
  return (char *)"Celestron AuxRemote Gateway";
}

bool AuxRemote::initProperties() {
  INDI::Telescope::initProperties();
  IUFillText(&httpEndpointT[0], "API_ENDPOINT", "API Endpoint", "http://localhost:8080/api");
  IUFillTextVector(&httpEndpointTP, httpEndpointT, 1, getDeviceName(), "HTTP_API_ENDPOINT", "HTTP endpoint", OPTIONS_TAB, IP_RW, 5, IPS_IDLE);
  TrackState=SCOPE_IDLE;
  initGuiderProperties(getDeviceName(), MOTION_TAB);
  addDebugControl();
  setDriverInterface(getDriverInterface() | GUIDER_INTERFACE);
  SetParkDataType(PARK_AZ_ALT);
  return true;
}

bool AuxRemote::saveConfigItems(FILE *fp) {
  DEBUG(INDI::Logger::DBG_ERROR, "**save conf");
  INDI::Telescope::saveConfigItems(fp);
  IUSaveConfigText(fp, &httpEndpointTP);
  return true;
}

void AuxRemote::ISGetProperties(const char *dev) {
  INDI::Telescope::ISGetProperties (dev);
  defineText(&httpEndpointTP);
}

bool AuxRemote::updateProperties() {
  DEBUG(INDI::Logger::DBG_ERROR, "**update props");
  INDI::Telescope::updateProperties();

  if (isConnected())
  {
    defineNumber(&GuideNSNP);
    defineNumber(&GuideWENP);


    if (InitPark()) {
        DEBUG(INDI::Logger::DBG_ERROR, "**initpark");
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
      // deleteProperty(EqPENV.name);
      // deleteProperty(PEErrNSSP.name);
      // deleteProperty(PEErrWESP.name);
      // deleteProperty(GuideRateNP.name);
  }

  return true;
}

bool AuxRemote::ISNewNumber (const char *dev, const char *name, double values[], char *names[], int n) {
    DEBUG(INDI::Logger::DBG_ERROR, "**ISNewNumber");
    if(strcmp(dev,getDeviceName())==0)
    {
        if (!strcmp(name,GuideNSNP.name) || !strcmp(name,GuideWENP.name))
        {
            processGuiderProperties(name, values, names, n);
            return true;
        }
    }

    return INDI::Telescope::ISNewNumber(dev, name, values, names, n);
}

bool AuxRemote::ISNewText(const char *dev, const char *name, char *texts[], char *names[], int n) {
  DEBUG(INDI::Logger::DBG_ERROR, "**ISNewText");
  if(!strcmp(dev, getDeviceName())) {
    if (!strcmp(httpEndpointTP.name, name)) {
        IUUpdateText(&httpEndpointTP, texts, names, n);
        httpEndpointTP.s = IPS_OK;
        IDSetText(&httpEndpointTP, NULL);
        return true;
    }
  }
  return Telescope::ISNewText(dev,name,texts,names,n);
}

bool AuxRemote::Connect() {
  bool connected = false;
  if (httpEndpointT[0].text == NULL)
  {
      DEBUG(INDI::Logger::DBG_ERROR, "HTTP API endpoint is not available. Set it in the options tab");
      return false;
  }
  DEBUGF(INDI::Logger::DBG_DEBUG,  "Updating SerialPort to %s...", PortT[0].text);
  char _json[60];
  snprintf(_json, 60, "{\"serialPort\":\"%s\"}", PortT[0].text);
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
  bool result = false;
  DEBUGF(INDI::Logger::DBG_DEBUG, "Reading status from %s",httpEndpointT[0].text);
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
        DEBUGF(INDI::Logger::DBG_DEBUG, "http response %s", readBuffer.c_str());
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
            if (!strcmp(it->key, "trackingState")) {
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
                DEBUG(INDI::Logger::DBG_SESSION, "status Slewing");
                TrackState = SCOPE_SLEWING;
                EqNP.s = IPS_BUSY;
              }
              if(strcmp(ts,"PARKING")==0) {
                DEBUG(INDI::Logger::DBG_SESSION, "status Parking");
                TrackState = SCOPE_PARKING;
                EqNP.s = IPS_BUSY;
              }
              if(strcmp(ts,"PARKED")==0) {
                DEBUG(INDI::Logger::DBG_SESSION, "Entering parked logic");
                if(TrackState == SCOPE_PARKING || TrackState == SCOPE_SLEWING) {
                  SetParked(true);
                  DEBUG(INDI::Logger::DBG_SESSION, "Park succesfull");
                  sleep(5);
                  TrackState = SCOPE_PARKED;
                  EqNP.s = IPS_OK;
                  DEBUG(INDI::Logger::DBG_SESSION, "Leavingg parked logic");
                }
              }
            }
        }
        DEBUG(INDI::Logger::DBG_SESSION, "Setting new RA DEC");
        NewRaDec(ra, dec);
        currentRA = ra;
        currentDEC = dec;
        result = true;
      }
    }
    DEBUG(INDI::Logger::DBG_SESSION, "**Leaving ReadScopeStatus");
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

void AuxRemote::SetCurrentPark() {
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
}

void AuxRemote::SetDefaultPark() {
  // By default set RA to HA
  SetAxis1Park(ln_get_apparent_sidereal_time(ln_get_julian_from_sys()));
  // Set DEC to 90 or -90 depending on the hemisphere
  SetAxis2Park( (LocationN[LOCATION_LATITUDE].value > 0) ? 90 : -90);
}


bool AuxRemote::UnPark() {
  DEBUG(INDI::Logger::DBG_ERROR, "**unpark");
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
