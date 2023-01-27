
enum Status { none, loading, complete, error }

class DataSource<T> {
  Status? status;
  T? data;
  String? message;

  DataSource(this.status, this.data);

  DataSource.none(): status = Status.none;

  DataSource.loading(): status = Status.loading;

  DataSource.complete(this.data): status = Status.complete;

  DataSource.error(this.message): status = Status.error;
}