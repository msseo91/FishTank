
import 'dart:core';

import 'package:fishtank/model/data_source.dart';
import 'package:fishtank/repository/fishtank_repository.dart';
import 'package:flutter/material.dart';

class SignInViewModel extends ChangeNotifier {
  final FishTankRepository repository = FishTankRepository();

  DataSource<bool> isSignIn = DataSource.none();

  _setSignIn(DataSource<bool> signInResult) {
    isSignIn = signInResult;
    notifyListeners();
  }

  Future<void> signIn(String id, String password) async {
     repository.signIn(id, password).then((value) => {
       if(value) { _setSignIn(DataSource.complete(value)) }
       else { _setSignIn(DataSource.error("Fail to signIn")) }
     });
  }
}
