#include <OneWire.h>
#include <DallasTemperature.h>

// Data wire is plugged into digital pin 2 on the Arduino
#define ONE_WIRE_BUS 52

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);  

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

void setup() {
  sensors.begin();  // Start up the library
  Serial.begin(9600);
}

void loop() {
   // Send the command to get temperatures
  sensors.requestTemperatures(); 

  //print the temperature in Celsius
  float temperature = sensors.getTempCByIndex(0);
  Serial.print("Temerature is ");
  Serial.println(temperature);
 
  delay(500);
  
}
