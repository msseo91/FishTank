//
// Created by MSSeo on 2022-08-31.
//

#ifndef FISHTANKNATIVE_FISHPACKET_H
#define FISHTANKNATIVE_FISHPACKET_H

#define STX 0x02
#define ETX 0x03
#define DLE 0x10

class FishPacket {
public:
    const unsigned char stx = STX;

    // Start Data
    long id = 0;
    long clientId = 0;
    int opCode = 0;
    int pin = 0;
    int pinMode = 0;
    float data = 0;
    // End Data

    // CheckSum
    unsigned int crc = 0;

    const unsigned char etx = ETX;

    /**
     * Write value to target in little endian order.
     * Assume value is variable.
     *
     * @param sizeOfValue
     * @param value value to write to target.
     * @param target target address in buffer.
     * @return count of byte written in this function.
     */
    int write(const unsigned char* value, int sizeOfValue, unsigned char* target) ;

    /**
     * Serialize this packet to byte array.
     *
     * @param bff buffer to serialize.
     * @return byte size of serialized packet.
     */
    int serializePacket(unsigned char bff[]);

    /**
     *
     * @param value
     * @param sizeOfValue
     * @param buffer
     * @return byte size of read from buffer.
     */
    int read(unsigned char* value, int sizeOfValue, const unsigned char* buffer);

    int deSerializePacket(unsigned char bff[]);

    void clear();

    bool validateCrc();

    unsigned int makeCrc();
};

void clearPacket(FishPacket &packet);

void sendPacket(FishPacket &packet);

void readPacket(FishPacket &packet);

#endif //FISHTANKNATIVE_FISHPACKET_H