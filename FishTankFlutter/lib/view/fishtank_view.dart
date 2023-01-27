import 'package:fishtank/string.dart';
import 'package:flutter/material.dart';

class FishTankPage extends StatefulWidget {
  static const String id = "fishtank";

  const FishTankPage({super.key});

  @override
  State createState() => FishTankState();
}

class FishTankState extends State<FishTankPage> {
  bool _switch1 = false;
  bool _switch2 = false;
  bool _switch3 = false;
  double _slider = 0.0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("FishTank"),
        ),
        body: Column(
          children: [
            SwitchListTile(
              value: _switch1,
              onChanged: (value) {
                setState(() {
                  _switch1 = value;
                });
              },
              title: const Text(Strings.textInValve),
            ),
            SwitchListTile(
              value: _switch2,
              onChanged: (value) {
                setState(() {
                  _switch2 = value;
                });
              },
              title: const Text(Strings.textOutValve),
            ),
            SwitchListTile(
              value: _switch3,
              onChanged: (value) {
                setState(() {
                  _switch3 = value;
                });
              },
              title: const Text(Strings.textInValve),
            ),
            Slider(
              value: _slider,
              onChanged: (value) {
                setState(() {
                  _slider = value;
                });
              },
            ),
          ],
        ));
  }
}
