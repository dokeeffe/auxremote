#ifndef AUXREMOTE_H
#define AUXREMOTE_H

#include "indiguiderinterface.h"
#include "inditelescope.h"
#include "indicontroller.h"

class AuxRemote : public INDI::Telescope, public INDI::GuiderInterface
{
public:
  AuxRemote();
  virtual ~AuxRemote();

  virtual const char *getDefaultName();
  virtual bool initProperties();
  virtual bool saveConfigItems(FILE *fp);

  void ISGetProperties(const char *dev);
  bool ISNewText(const char *dev, const char *name, char *texts[], char *names[], int n);
  bool ISNewNumber (const char *dev, const char *name, double values[], char *names[], int n);
  bool ISNewSwitch (const char *dev, const char *name, ISState *states, char *names[], int n);

  virtual bool Connect();
  virtual bool Disconnect();
  virtual bool updateProperties();

protected:

  virtual bool MoveNS(INDI_DIR_NS dir, TelescopeMotionCommand command);
  virtual bool MoveWE(INDI_DIR_WE dir, TelescopeMotionCommand command);
  virtual bool Abort();

  bool ReadScopeStatus();
  bool Goto(double,double);
  bool Sync(double ra, double dec);

  //GUIDE: guiding functions
  virtual IPState GuideNorth(uint32_t ms);
  virtual IPState GuideSouth(uint32_t ms);
  virtual IPState GuideEast(uint32_t ms);
  virtual IPState GuideWest(uint32_t ms);

  bool Park();
  bool UnPark();
  virtual bool SetCurrentPark();
  virtual bool SetDefaultPark();

private:

  IText CurrentStateMsgT[1];
  ITextVectorProperty CurrentStateMsgTP;

  bool SendPostRequest(const char *json_payload,const char *path);
  double currentRA, currentDEC;
  bool pecIndexFound;

  /* API endpoint */
  IText httpEndpointT[1];
  ITextVectorProperty httpEndpointTP;

  /* API endpoint serial config */
  IText auxRemoteSerialDeviceT[1];
  ITextVectorProperty auxRemoteSerialDeviceTP;

  /* PEC */
  IText PecT[1];
  ITextVectorProperty PecTP;
  ISwitch PecModeS[4];
  ISwitchVectorProperty PecModeSP;

};

#endif
