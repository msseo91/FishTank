//
// Created by MSSeo on 2022-10-11.
//

#ifndef FISHTANKNATIVE_FISHTANK_H
#define FISHTANKNATIVE_FISHTANK_H

#include "FishPacket.h"
#include "Arduino.h"

class FishTank {
public:
    Ser Serial;
    Ser Serial1;

    void clearPacket(FishPacket &packet);
    void printArrayAsHex(unsigned char arr[], int len);
    void printFishPacket(FishPacket &packet);
    void sendPacket(FishPacket &packet);
    void readPacket(FishPacket &packet);

};

#endif //FISHTANKNATIVE_FISHTANK_H
