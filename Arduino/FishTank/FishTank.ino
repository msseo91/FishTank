#include "DallasTemperature.h"
#include "Arduino.h"
#include "FishPacket.h"
#include <SoftwareSerial.h>

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
#define PACKET_SIZE_MAX 40

#define PIN_LENGTH 53

#define RX 9
#define TX 8

SoftwareSerial Serial1 = SoftwareSerial(RX, TX);

// Setup a oneWire instance to communicate with any OneWire device
OneWire oneWire(ONE_WIRE_BUS);

// Pass oneWire reference to DallasTemperature library
DallasTemperature sensors(&oneWire);

unsigned long prevMils = 0;

int pinState[PIN_LENGTH];

void clearPacket(FishPacket &packet) {
    packet.id = 0;
    packet.clientId = 0;
    packet.opCode = 0;
    packet.pin = 0;
    packet.pinMode = 0;
    packet.data = 0;
}

void printArrayAsHex(unsigned char arr[], int len) {
    // Print buffer for debugging
    for (int i=0; i<len; i++) {
        char hexBuf[3];
        sprintf(hexBuf, "%X", i);
        hexBuf[2] = 0;

        Serial1.print(hexBuf);
        Serial1.print(" ");
    }
}

void printFishPacket(FishPacket &packet) {
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

void sendPacket(FishPacket &packet) {
    Serial1.println("Send Packet");

    unsigned char buffer[PACKET_SIZE_MAX];
    memset(buffer, 0, PACKET_SIZE_MAX);

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

void readPacket(FishPacket &packet) {
    char firstByte = Serial.read();
    if(firstByte == STX) {
        // Packet received
        unsigned char buffer[PACKET_SIZE_MAX];
        memset(buffer, 0, PACKET_SIZE_MAX);

        int idx = 0;
        bool escaping = false;
        for(int i= 0; i<PACKET_SIZE_MAX; i++) {
            unsigned char b = Serial.read();
            if(escaping) {
                // This byte is escaped. alloc unconditionally.
                buffer[idx++] = b;
                escaping = false;
                continue;
            }

            if(b == DLE) {
                // Next byte is escaped!
                escaping = true;
                continue;
            }

            if(b == ETX){
                // End of packet.
                buffer[idx++] = b;
                // Read CRC
                buffer[idx++] = Serial.read();
                buffer[idx++] = Serial.read();
                break;
            }

            buffer[idx++] = b;
        }

        packet.deSerializePacket(buffer);
        if(!packet.validateCrc()) {
            packet.clear();
        }
    }
}

void setup() {
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

void loop() {
    unsigned long currentMils = millis();

    if (currentMils - prevMils > LOOP_INTERVAL) {
        FishPacket packet;
        
        readPacket(packet);

        if (packet.id != 0) {
            switch (packet.opCode) {
                case OP_GET_TEMPERATURE: {
                    sensors.requestTemperatures();
                    float temperature = sensors.getTempCByIndex(0);

                    packet.data = temperature;
                    break;
                }
                case OP_INPUT_PIN: {
                    pinMode(packet.pin, packet.pinMode);
                    int value = (int) (packet.data);

                    digitalWrite(packet.pin, value);

                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        pinState[packet.pin] = value;
                    }

                    break;
                }
                case OP_READ_DIGIT_PIN: {
                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        packet.data = pinState[packet.pin];
                    }

                    break;
                }
                case OP_INPUT_ANALOG_PIN: {
                    pinMode(packet.pin, packet.pinMode);
                    int value = (int) (packet.data);
                    analogWrite(packet.pin, value);

                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        pinState[packet.pin] = value;
                    }

                    break;
                }
                case OP_READ_ANALOG_PIN: {
                    if (packet.pin < PIN_LENGTH && packet.pin >= 0) {
                        packet.data = pinState[packet.pin];
                    }
                    break;
                }
            }

            // Send response
            sendPacket(packet);
        }

        // clear buffer
        clearPacket(packet);

        // Update prevMils.
        prevMils = millis();
    }
}
