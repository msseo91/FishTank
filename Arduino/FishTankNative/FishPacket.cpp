//
// Created by MSSeo on 2022-08-31.
//
#include "FishPacket.h"



/**
 * Write value to target in little endian order.
 * Assume value is variable.
 *
 * @param sizeOfValue
 * @param value value to write to target.
 * @param target target address in buffer.
 * @return count of byte written in this function.
 */
int FishPacket::write(const unsigned char* value, int sizeOfValue, unsigned char* target) {
    int idx = 0;

    for(int i=0; i<sizeOfValue; i++) {
        // Meta data should be escaped.
        switch (value[i]) {
            case STX:
            case ETX:
            case DLE:
                // Add DLE for escape.
                target[idx++] = DLE;
                break;
        }

        target[idx++] = value[i];
    }

    return idx;
}

/**
 * Serialize this packet to byte array.
 *
 * @param bff buffer to serialize.
 * @return byte size of serialized packet.
 */
int FishPacket::serializePacket(unsigned char bff[]) {
    // Little-endian
    int index = 0;
    bff[index++] = STX;

    crc = makeCrc();

    index += write((unsigned char *)&id, 4, bff+index);
    index += write((unsigned char *)&clientId, 4, bff+index);
    index += write((unsigned char *)&opCode, 2, bff+index);
    index += write((unsigned char *)&pin, 2, bff+index);
    index += write((unsigned char *)&pinMode, 2, bff+index);
    index += write((unsigned char *)&data, 4, bff+index);

    bff[index++] = ETX;
    unsigned char* cp = (unsigned char *)&crc;
    bff[index++] = cp[0];
    bff[index++] = cp[1];

    return index;
}

/**
 *
 * @param value
 * @param sizeOfValue
 * @param buffer
 * @return byte size of read from buffer.
 */
int FishPacket::read(unsigned char* value, int sizeOfValue, const unsigned char* buffer) {
    int idx = 0;
    int vIdx = 0;

    bool escaped = false;
    while(true) {
        unsigned char b = buffer[idx++];
        if(!escaped && b == DLE) {
            escaped = true;
            continue;
        }

        escaped = false;
        value[vIdx++] = b;
        if(vIdx >= sizeOfValue) break;
    }
    return idx;
}

int FishPacket::deSerializePacket(unsigned char bff[]) {
    // Little-endian
    int index = 0;
    if(bff[index++] != STX) return -1;

    index += read((unsigned char *)&id, 4, bff+index);
    index += read((unsigned char *)&clientId, 4, bff+index);
    index += read((unsigned char *)&opCode, 2, bff+index);
    index += read((unsigned char *)&pin, 2, bff+index);
    index += read((unsigned char *)&pinMode, 2, bff+index);
    index += read((unsigned char *)&data, 4, bff+index);
    index += read((unsigned char *)&crc, 2, bff+index);

    if(bff[index++] != ETX) return -1;

    return index;
}

void FishPacket::clear() {
    id = 0;
    clientId = 0;
    opCode = 0;
    pin = 0;
    pinMode = 0;
    data = 0;
    crc = 0;
}

bool FishPacket::validateCrc() {
    return makeCrc() == crc;
}

unsigned int FishPacket::makeCrc() {
    int localCrc = (id << 12) & 0xF000;
    localCrc |= (clientId << 8) & 0x0F00;
    localCrc |= (pin << 4) & 0x00F0;
    localCrc |= pinMode & 0x000F;
    return localCrc;
}

