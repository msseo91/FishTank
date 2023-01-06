import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  bool _switch1 = false;
  bool _switch2 = false;
  bool _switch3 = false;
  double _slider = 0.0;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Column(
          children: [
            SwitchListTile(
              value: _switch1,
              onChanged: (value) {
                setState(() {
                  _switch1 = value;
                });
              },
              title: Text('Switch 1'),
            ),
            SwitchListTile(
              value: _switch2,
              onChanged: (value) {
                setState(() {
                  _switch2 = value;
                });
              },
              title: Text('Switch 2'),
            ),
            SwitchListTile(
              value: _switch3,
              onChanged: (value) {
                setState(() {
                  _switch3 = value;
                });
              },
              title: Text('Switch 3'),
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
        ),
      ),
    );
  }
}
