#include "OneWire.h"

typedef uint8_t DeviceAddress[8];

class DallasTemperature {
public:

	DallasTemperature();
	DallasTemperature(OneWire*);
	DallasTemperature(OneWire*, uint8_t);

    void setOneWire(OneWire*);

    void setPullupPin(uint8_t);

	// initialise bus
	void begin();

	// returns the number of devices found on the bus
	uint8_t getDeviceCount(void);

	// returns the number of DS18xxx Family devices on bus
	uint8_t getDS18Count(void);

	// returns true if address is valid
	bool validAddress(const uint8_t*);

	// returns true if address is of the family of sensors the lib supports.
	bool validFamily(const uint8_t* deviceAddress);

	// finds an address at a given index on the bus
	bool getAddress(uint8_t*, uint8_t);

	// attempt to determine if the device at the given address is connected to the bus
	bool isConnected(const uint8_t*);

	// attempt to determine if the device at the given address is connected to the bus
	// also allows for updating the read scratchpad
	bool isConnected(const uint8_t*, uint8_t*);

	// read device's scratchpad
	bool readScratchPad(const uint8_t*, uint8_t*);

	// write device's scratchpad
	void writeScratchPad(const uint8_t*, const uint8_t*);

	// read device's power requirements
	bool readPowerSupply(const uint8_t* deviceAddress = nullptr);

	// get global resolution
	uint8_t getResolution();

	// set global resolution to 9, 10, 11, or 12 bits
	void setResolution(uint8_t);

	// returns the device resolution: 9, 10, 11, or 12 bits
	uint8_t getResolution(const uint8_t*);

	// set resolution of a device to 9, 10, 11, or 12 bits
	bool setResolution(const uint8_t*, uint8_t,
			bool skipGlobalBitResolutionCalculation = false);

	// sets/gets the waitForConversion flag
	void setWaitForConversion(bool);
	bool getWaitForConversion(void);

	// sets/gets the checkForConversion flag
	void setCheckForConversion(bool);
	bool getCheckForConversion(void);

	// sends command for all devices on the bus to perform a temperature conversion
	void requestTemperatures(void);

	// sends command for one device to perform a temperature conversion by address
	bool requestTemperaturesByAddress(const uint8_t*);

	// sends command for one device to perform a temperature conversion by index
	bool requestTemperaturesByIndex(uint8_t);

	// returns temperature raw value (12 bit integer of 1/128 degrees C)
	int16_t getTemp(const uint8_t*);

	// returns temperature in degrees C
	float getTempC(const uint8_t*);

	// returns temperature in degrees F
	float getTempF(const uint8_t*);

	// Get temperature for device index (slow)
	float getTempCByIndex(uint8_t);

	// Get temperature for device index (slow)
	float getTempFByIndex(uint8_t);

	// returns true if the bus requires parasite power
	bool isParasitePowerMode(void);

	// Is a conversion complete on the wire? Only applies to the first sensor on the wire.
	bool isConversionComplete(void);

  int16_t millisToWaitForConversion(uint8_t);
  
  // Sends command to one device to save values from scratchpad to EEPROM by index
  // Returns true if no errors were encountered, false indicates failure
  bool saveScratchPadByIndex(uint8_t);
  
  // Sends command to one or more devices to save values from scratchpad to EEPROM
  // Returns true if no errors were encountered, false indicates failure
  bool saveScratchPad(const uint8_t* = nullptr);
  
  // Sends command to one device to recall values from EEPROM to scratchpad by index
  // Returns true if no errors were encountered, false indicates failure
  bool recallScratchPadByIndex(uint8_t);
  
  // Sends command to one or more devices to recall values from EEPROM to scratchpad
  // Returns true if no errors were encountered, false indicates failure
  bool recallScratchPad(const uint8_t* = nullptr);
  
  // Sets the autoSaveScratchPad flag
  void setAutoSaveScratchPad(bool);
  
  // Gets the autoSaveScratchPad flag
  bool getAutoSaveScratchPad(void);

};

