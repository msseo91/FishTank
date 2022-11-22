#include "DallasTemperature.h"
// #include "Arduino.h"
#include "FishPacket.h"
#include "FishTank.h"
// #include <SoftwareSerial.h>

FishTank fishTank;

void setup() {
    fishTank.init();
}

unsigned long prevMils = 0;
void loop() {
    unsigned long currentMils = millis();

    if (currentMils - prevMils > LOOP_INTERVAL) {
        FishPacket packet;
        fishTank.readPacket(packet);

        if (packet.id != 0) {
            switch (packet.opCode) {
                case OP_GET_TEMPERATURE: {
                    packet.data = fishTank.readTemperature();
                    break;
                }
                case OP_INPUT_PIN: {
                    fishTank.writeDigit(packet.pin, (int)packet.data);
                    break;
                }
                case OP_READ_DIGIT_PIN: {
                    packet.data = fishTank.readDigit(packet.pin);
                    break;
                }
                case OP_INPUT_ANALOG_PIN: {
                    fishTank.writeAnalog(packet.pin, (int) packet.data);
                    break;
                }
                case OP_READ_ANALOG_PIN: {
                    packet.data = fishTank.readAnalog(packet.pin);
                    break;
                }
            }

            // Send response
            fishTank.sendPacket(packet);
        }

        // clear buffer
        fishTank.clearPacket(packet);

        // Update prevMils.
        prevMils = millis();
    }
}
