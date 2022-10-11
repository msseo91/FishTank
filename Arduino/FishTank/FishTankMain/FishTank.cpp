#include "DallasTemperature.h"
#include "FishPacket.h"
#include "FishTank.h"


// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

int pinState[PIN_LENGTH];

void FishTank::init() {
    // Clear pinState
    for (int state: pinState) {
        state = 0;
    }
    
    Serial.begin(BAUD_RATE);
    Serial.setTimeout(READ_TIMEOUT);
    Serial.flush();

    Serial1.begin(BAUD_RATE);
    Serial1.println("Setup Software Serial!");
    sensors.begin();
}

void FishTank::clearPacket(FishPacket &packet) {
    packet.id = 0;
    packet.clientId = 0;
    packet.opCode = 0;
    packet.pin = 0;
    packet.pinMode = 0;
    packet.data = 0;
}

void FishTank::printArrayAsHex(unsigned char arr[], int len) {
    // Print buffer for debugging
    for (int i=0; i<len; i++) {
        char hexBuf[5];
        sprintf(hexBuf, "0x%X", arr[i]);
        hexBuf[4] = 0;

        Serial1.print(hexBuf);
        Serial1.print(" ");
    }
    Serial1.println();
}

void FishTank::printFishPacket(FishPacket &packet) {
    Serial1.println();
    Serial1.print(", id=");
    Serial1.print(packet.id);
    Serial1.print(", clientId=");
    Serial1.print(packet.clientId);
    Serial1.print(", opCode=");
    Serial1.print(packet.opCode);
    Serial1.print(", pin=");
    Serial1.print(packet.pin);
    Serial1.print(", pinMode=");
    Serial1.print(packet.pinMode);
    Serial1.print(", data=");
    Serial1.print(packet.data);
    Serial1.println();
}

void FishTank::sendPacket(FishPacket &packet) {
    Serial1.println("Send Packet");

    unsigned char buffer[PACKET_SIZE];
    memset(buffer, 0, PACKET_SIZE);

    int size = packet.serializePacket(buffer);

    // Print buffer for debugging
    printArrayAsHex(buffer, size);
    printFishPacket(packet);

    // Send packet
    Serial.write(buffer, size);
    Serial.flush();

    Serial1.println("Send packet Complete");
    Serial1.println();
}

void FishTank::readPacket(FishPacket &packet) {
    unsigned char buffer[PACKET_SIZE];
    memset(buffer, 0, PACKET_SIZE);

    int idx = 0;
    while(true) {
      int v = Serial.read();
      if(v == -1) continue;
      buffer[idx++] = v;
      printArrayAsHex(buffer, idx);
      if(idx >= PACKET_SIZE) break;
    }

    printArrayAsHex(buffer, PACKET_SIZE);
    /*
    size_t readSize= Serial.readBytes(buffer, PACKET_SIZE);

    if(readSize <= 0) {
      Serial1.println("Nothing to read!");
      return;
    }
    if(readSize != PACKET_SIZE) {
      Serial1.print("Wrong size...");
      Serial1.print(" Read=");
      Serial1.println(readSize);
      printArrayAsHex(buffer, readSize);
      return;
    }
    */

    idx = 0;
    if(buffer[0] == STX) {
        // Packet received
        Serial1.println("Packet Received!");
        packet.deSerializePacket(buffer);

        if(!packet.validateCrc()) {
            unsigned int expectedCrc = packet.makeCrc();
            Serial1.println("CRC is not match!");
            Serial1.print("Expected ");
            Serial1.print(expectedCrc);
            Serial1.print(" But ");
            Serial1.println(packet.crc);
            printFishPacket(packet);
            packet.clear();
        }
    }
}

float FishTank::readTemperature() {
  sensors.requestTemperatures();
  return sensors.getTempCByIndex(0);
}

void FishTank::writeDigit(int pin, int value) {
   pinMode(pin, OUTPUT);
   digitalWrite(pin, value);
   if (pin < PIN_LENGTH && pin >= 0) {
      pinState[pin] = value;
   }
}

int FishTank::readDigit(int pin) {
  if (pin < PIN_LENGTH && pin >= 0) {
     return pinState[pin];
  }
  return -1;
}

void FishTank::writeAnalog(int pin, int value) {
  pinMode(pin, OUTPUT);
  analogWrite(pin, value);
  if (pin < PIN_LENGTH && pin >= 0) {
      pinState[pin] = value;
  }
}

int FishTank::readAnalog(int pin) {
  if (pin < PIN_LENGTH && pin >= 0) {
      return pinState[pin];
   }
   return -1;
}
