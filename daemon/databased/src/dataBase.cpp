#include "dataBase.h"

int main(int argc, char **argv)
{
  std::cout << "databased started" << std::endl;
  
  database::RunServer();

  return 0;
}

/************ FUNCTION DEFINITIONS ***************************************************************/
void database::RunServer(void)
{
  std::cout << "RunServer" << std::endl;
  std::string server_address("0.0.0.0:50051");
  //RouteGuideImpl service(db_path);

  grpc::ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
  //builder.RegisterService(&service);
  //std::unique_ptr<Server> server(builder.BuildAndStart());
  //std::cout << "Server listening on " << server_address << std::endl;
  //server->Wait();
}