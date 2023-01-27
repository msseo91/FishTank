import 'package:fishtank/view/fishtank_view.dart';
import 'package:fishtank/view/signin_view.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      initialRoute: SignInPage.id,
      routes: {
        SignInPage.id : (context) => const SignInPage(),
        FishTankPage.id: (context) => const FishTankPage()
      },
    );
  }
}
