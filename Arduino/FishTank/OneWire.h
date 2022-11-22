#include <cstdint>

class OneWire
{
  private:
  public:
    OneWire() = default;
    explicit OneWire(uint8_t pin) { begin(pin); }
    void begin(uint8_t pin);
};
