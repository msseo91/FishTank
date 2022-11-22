//
// Created by MSSeo on 2022-08-25.
//

#include "Arduino.h"

uint8_t mData[100];
int index = 0;

void Ser::println() {
    printf("\n");
}

void Ser::println(const char *s) {
    printf(s);
}

void Ser::print() {}

void Ser::print(const char *s) {
    printf(s);
}

void Ser::print(int) {}

void Ser::print(float) {}

void Ser::print(double) {}

void Ser::print(long) {}

void Ser::write(uint8_t[], int) {}

void Ser::flush() {}

void Ser::readBytes(uint8_t[], int) {}

void Ser::begin(int) {}

void Ser::setTimeout(int) {}

int Ser::read() {
    return mData[index++];
}

void Ser::mockSetReadNext(uint8_t data[], int size) {
    memset(mData, 0, 100);
    memcpy(mData, data, size);
    index = 0;
}

unsigned long millis(){ }
void pinMode(int, int){ }
void digitalWrite(int, int){ }
void analogWrite(int, int){ }