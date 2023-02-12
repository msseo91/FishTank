import 'package:fishtank/string.dart';
import 'package:flutter/material.dart';

class FishTankPage extends StatefulWidget {
  static const String id = "fishtank";

  const FishTankPage({super.key});

  @override
  State createState() => FishTankState();
}

class FishTankState extends State<FishTankPage> {
  bool _inValveState = false;
  bool _outValveState = false;
  bool _heaterState = false;
  double _lightValue = 0.0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text("FishTank"),
        ),
        body: Column(
          children: [
            SwitchListTile(
              value: _inValveState,
              onChanged: (value) {
                setState(() {
                  _inValveState = value;
                });
              },
              title: const Text(Strings.textInValve),
            ),
            SwitchListTile(
              value: _outValveState,
              onChanged: (value) {
                setState(() {
                  _outValveState = value;
                });
              },
              title: const Text(Strings.textOutValve),
            ),
            SwitchListTile(
              value: _heaterState,
              onChanged: (value) {
                setState(() {
                  _heaterState = value;
                });
              },
              title: const Text(Strings.textHeater),
            ),

            Container(
              margin: const EdgeInsets.only(top: 25.0, left: 10.0) ,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const Text(
                    style: TextStyle(fontSize: 17),
                      Strings.textLight
                  ),
                  Slider(
                    label: "${Strings.textLight} (${_lightValue.round()} %)",
                    min: 0,
                    max: 100,
                    divisions: 100,
                    value: _lightValue,
                    onChanged: (value) {
                      setState(() {
                        _lightValue = value;
                      });
                    },
                  ),
                ],
              )
            )


          ],
        ));
  }
}
