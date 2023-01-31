import 'dart:core';

import 'package:fishtank/model/data_source.dart';
import 'package:fishtank/repository/fishtank_repository.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../model/user.dart';

class SignInViewModel extends ChangeNotifier {
  final FishTankRepository _repository = FishTankRepository();
  DataSource<bool> isSignIn = DataSource.none();
  User user = User(id: "", password: "");

  SignInViewModel() {
    getSavedUser().then((value) => {user = value, notifyListeners()});
  }

  _setSignIn(DataSource<bool> signInResult) {
    isSignIn = signInResult;
    notifyListeners();
  }

  _saveUser(String id, String password) async {
    final prefs = await SharedPreferences.getInstance();

    prefs.setString('id', id);
    prefs.setString('password', password);
  }

  Future<User> getSavedUser() async {
    final prefs = await SharedPreferences.getInstance();
    return User(
        id: prefs.getString("id") ?? "",
        password: prefs.getString("password") ?? "");
  }

  Future<void> signIn(String id, String password) async {
    _repository.signIn(id, password).then((value) => {
          if (value)
            {_saveUser(id, password), _setSignIn(DataSource.complete(value))}
          else
            {_setSignIn(DataSource.error("Fail to signIn"))}
        });
  }
}
