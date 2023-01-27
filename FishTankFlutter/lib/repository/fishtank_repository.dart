
import 'package:fishtank/api/fish_datasource.dart';
import 'package:dio/dio.dart';
import 'package:logger/logger.dart';

class FishTankRepository {
  late final FishTankApi dataSource;
  var logger = Logger();
  String token = "";

  FishTankRepository() {
    dataSource = FishTankApi(Dio(), baseUrl: "http://fish.marineseo.xyz:8080");
  }

  Future<bool> signIn(String id, String password) async {
    token = await dataSource.signIn(id, password).catchError((error) {
      logger.e(error);
      return "";
    });
    return token.isNotEmpty;
  }
}