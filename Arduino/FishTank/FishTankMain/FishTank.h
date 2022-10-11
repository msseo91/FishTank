//
// Created by MSSeo on 2022-10-11.
//

#ifndef FISHTANKNATIVE_FISHTANK_H
#define FISHTANKNATIVE_FISHTANK_H

#include <SoftwareSerial.h>
#include "FishPacket.h"
#include "Arduino.h"

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
#define RX 9
#define TX 8

class FishTank {
public:
    SoftwareSerial Serial1 = SoftwareSerial(RX, TX);;

    void init();
    void clearPacket(FishPacket &packet);
    void printArrayAsHex(unsigned char arr[], int len);
    void printFishPacket(FishPacket &packet);
    void sendPacket(FishPacket &packet);
    void readPacket(FishPacket &packet);

    // Functions
    float readTemperature();
    void writeDigit(int pin, int value);
    int readDigit(int pin);
    void writeAnalog(int pin, int value);
    int readAnalog(int pin);
};

#endif //FISHTANKNATIVE_FISHTANK_H
