//
// Created by MSSeo on 2022-08-25.
//

#ifndef FISHTANKNATIVE_ARDUINO_H
#define FISHTANKNATIVE_ARDUINO_H

#include <cstdio>
#include <cstring>
// #include <rpcndr.h>
#include <cstdint>



class Ser {
public:
    static void println();
    static void println(const char*);

    static void print();
    static void print(const char*);
    static void print(int);
    static void print(float);
    static void print(double);
    static void print(long);

    static void write(uint8_t[], int);
    static void flush();
    static void readBytes(uint8_t[], int);

    static void begin(int);
    static void setTimeout(int);
    static int read();

    static void mockSetReadNext(uint8_t[], int size);
};

unsigned long millis();
void pinMode(int, int);
void digitalWrite(int, int);
void analogWrite(int, int);

void setup();
void loop();




#endif //FISHTANKNATIVE_ARDUINO_H
