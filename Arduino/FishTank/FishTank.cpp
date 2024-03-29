#include "DallasTemperature.h"
#include "FishPacket.h"
#include "FishTank.h"

#define READ_TIMEOUT 5000

#define ONE_WIRE_BUS 52
#define BAUD_RATE 57600

#define OP_GET_TEMPERATURE 1000
#define OP_INPUT_PIN 1001
#define OP_READ_DIGIT_PIN 1002
#define OP_INPUT_ANALOG_PIN 1003
#define OP_READ_ANALOG_PIN 1004

#define LOOP_INTERVAL 10

#define BUFFER_SIZE 512
#define SMALL_BUF_SIZE 256

#define MAGIC 31256
#define PACKET_SIZE 22

#define PIN_LENGTH 53

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

unsigned long prevMils = 0;

int pinState[PIN_LENGTH];


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
        char hexBuf[3];
        sprintf(hexBuf, "%X", i);
        hexBuf[2] = 0;

        Serial1.print(hexBuf);
        Serial1.print(" ");
    }
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
    uint8_t firstByte = Serial.read();
    int idx = 0;
    if(firstByte == STX) {
        // Packet received
        unsigned char buffer[PACKET_SIZE];
        memset(buffer, 0, PACKET_SIZE);

        buffer[idx++] = firstByte;
        for(int i= 0; i<PACKET_SIZE-1; i++) {
            buffer[idx++] = Serial.read();
        }

        packet.deSerializePacket(buffer);

        if(!packet.validateCrc()) {
            Serial1.println("CRC is not match!");
            packet.clear();
        }
    }
}


