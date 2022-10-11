#include <iostream>
#include <iomanip>
#include <cstring>
#include "FishPacket.h"

using namespace std;

void printPacket(FishPacket* packet) {
    cout << "id=" << packet->id << " clientId=" << packet->clientId <<
        " opCode=" << packet->opCode <<
         " pin=" << packet->pin << " pinMode=" << packet->pinMode <<
         " data=" << packet->data << " crc=" << packet->crc <<
         " valid=" << packet->validateCrc() <<
         endl;
}

void printBytes(unsigned char arr[], int len) {
    for(int i=0; i<=len; i++) {
        cout << hex << setfill('0') << setw(2) << (int)arr[i] << " ";
    }
    cout << endl;

    cout << dec;
}

int main() {
    for(int i=0; i<10; i++) {
        auto *fishPacket = new FishPacket();
        fishPacket->clientId = i;
        fishPacket->id = i+100;
        fishPacket->pin = i+299;
        fishPacket->pinMode = i;
        fishPacket->opCode = i;
        fishPacket->data = i * 0.75;

        unsigned int crc = fishPacket->makeCrc();
        cout << "CRC=" << crc << endl;

        unsigned char buffer[40];
        int packetSize = fishPacket->serializePacket(buffer);
        cout << "PacketSize=" << packetSize << endl;
        printPacket(fishPacket);
        printBytes(buffer, packetSize);

        auto *aPacket = new FishPacket();
        aPacket->deSerializePacket(buffer);
        printPacket(aPacket);
    }

    return 0;
}

