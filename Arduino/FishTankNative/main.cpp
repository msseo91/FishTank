#include <iostream>
#include <iomanip>
#include "Arduino.h"
#include "FishTank.h"

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
    for(int i=0; i<len; i++) {
        cout << "0x" << hex << setfill('0') << setw(2) << (int)arr[i] << ", ";
    }
    cout << endl;

    cout << dec;
}

void deSerializeTest() {
    cout << "=== Deserialize Test ===" << endl;
    uint8_t buffer[] = {0x02,
                        0x02, 0x00, 0x00, 0x00,
                        0x01, 0x00, 0x00, 0x00,
                        0xc8, 0x00,
                        0x0a, 0x00,
                        0x01, 0x00,
                        0x00, 0x00, 0x60, 0x40,
                        0xa1, 0x21,
                        0x03
    };

    FishPacket fishPacket;
    printPacket(&fishPacket);

    bool readResult = fishPacket.deSerializePacket(buffer) > 0;
    cout << "readResult=" << readResult << endl;

    bool isValid = fishPacket.validateCrc();
    cout << "isValid=" << isValid << endl;

    int calCrc = fishPacket.makeCrc();
    cout << "Calculated crc=" << calCrc << endl;

    printPacket(&fishPacket);

    cout << endl;
}

int main() {
    deSerializeTest();

    cout << "=== Serialize Test ===" << endl;
    FishPacket fishPacket;
    fishPacket.id = 1515;
    fishPacket.clientId = 2999;
    fishPacket.pin = 50;
    fishPacket.pinMode = 2;
    fishPacket.opCode = 3;
    fishPacket.crc = fishPacket.makeCrc();

    cout << "Before Serialize...";
    printPacket(&fishPacket);

    uint8_t buffer[22];
    fishPacket.serializePacket(buffer);

    FishPacket deserialized;
    deserialized.deSerializePacket(buffer);
    cout << "After Deserialize...";
    printPacket(&deserialized);

    printBytes(buffer, 22);
    cout << endl;

    cout << "===Read packet from Serial===" << endl;
    FishTank fishTank;
    fishTank.Serial.mockSetReadNext(buffer, 22);

    FishPacket readPacket;
    fishTank.readPacket(readPacket);
    printPacket(&readPacket);

    return 0;
}

/*
void setup() {
    // Clear pinState
    for (int state: pinState) {
        state = 0;
    }

    Serial.begin(BAUD_RATE);
    Serial.setTimeout(READ_TIMEOUT);
    Serial.flush();

    Serial1.begin(BAUD_RATE);
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
 */

