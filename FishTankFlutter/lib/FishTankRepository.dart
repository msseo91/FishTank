
import 'package:fishtank/FishDataSource.dart';

class FishTankRepository {
  late final FishTankApi dataSource;

  FishTankRepository() {
    dataSource = FishTankApi(baseUrl: "http://fish.marineseo.xyz:53265");
  }
}