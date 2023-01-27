import 'package:dio/dio.dart';
import 'package:retrofit/retrofit.dart';

part 'fish_datasource.g.dart';

class Key {
  static const String token = "token";
  static const String enable = "enable";
  static const String id = "id";
  static const String password = "password";
  static const String days = "days";
  static const String percentage = "percentage";
  static const String periodic = "periodicTask";
  static const String type = "type";
  static const String data = "data";
  static const String time = "time";
}


@RestApi(baseUrl: "http://fish.marineseo.xyz:8080")
abstract class FishTankApi {
  // API class
  factory FishTankApi(Dio dio, {String baseUrl}) = _FishTankApi;

  @POST("/fish/signin")
  @FormUrlEncoded()
  Future<String> signIn(@Field(Key.id) String id, @Field(Key.password) String password);
}


