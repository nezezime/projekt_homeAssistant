#include "dataBase.h"

/************ FUNCTION PROTOTYPES ****************************************************************/


using grpc::Server;
using grpc::ServerContext;
using grpc::ServerReader;
using grpc::ServerReaderWriter;
using grpc::ServerWriter;
using grpc::Status;
using databaseRPC::dbRPC;

/************ CLASS DEFINITIONS ******************************************************************/

/**
 * Implements all the service methods described in storage.proto by inheriting the Service class
 * @note: RPC methods are called from multiple threads at the same, the implementation must be
 * thread safe!
 */
class dbRPCImpl final : public dbRPC::Service
{
  //RPC Hello
  Status Hello(::grpc::ClientContext* context,
              const ::databaseRPC::HelloRequest& request,
              ::databaseRPC::HelloReply* response)
  {
    //TODO how to check if proto parameters are included in message

    int message_id = request.message_id();
    std::cout << "RPC server Hello id " << message_id << std::endl;
    response->set_message_id(message_id);
    response->set_message("Server Hello");
    return Status::OK;
  }
};

int main(int argc, char **argv)
{
  std::cout << "databased started" << std::endl;

  database::RunServer();

  return 0;
}

/************ FUNCTION DEFINITIONS ***************************************************************/
void database::RunServer(void)
{
  std::string server_address("0.0.0.0:50051");
  std::cout << "Starting databased RPC server @" << server_address << std::endl;

  //instantaniation of RPC service implementation class
  //TODO v njihovem primeru so parsali se nek db file -> preveri, ce pride v postev zate
  dbRPCImpl service;

  grpc::ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
  builder.RegisterService(&service);
  std::unique_ptr<Server> server(builder.BuildAndStart());
  std::cout << "Server listening on " << server_address << std::endl;

  //blocking wait until the process is killed
  server->Wait();
}
