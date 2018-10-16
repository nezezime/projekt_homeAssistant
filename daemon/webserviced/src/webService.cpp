#include "webService.h"

/************************************* FUNCTION PROTOTYPES ***************************************/


using grpc::Channel;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::ClientReaderWriter;
using grpc::ClientWriter;
using grpc::Status;
using databaseRPC::dbRPC;

/************************************** CLASS DEFINITIONS *****************************************/
class ClientDbRPC
{
public:
  ClientDbRPC(std::shared_ptr<Channel> channel) : stub_(dbRPC::NewStub(channel))
  {
    //TODO add db parse if required
    //std::cout << "client constructor" << std::endl;
  }

  void RPC_Hello(void)
  {
    databaseRPC::HelloRequest request;
    databaseRPC::HelloReply reply;

    //populate request
    request.set_name("test");
    request.set_message_id(1234);

    //send RPC
    int result = RPC_Hello_Execute(request, &reply);
    if(result != 0)
    {
      std::cout << "RPC failed with error code " << result << std::endl;
      return;
    }

    //parse response
    std::cout << "RPC response OK for message_id " << reply.message_id() << std::endl;
  }

private:
  std::unique_ptr<dbRPC::Stub> stub_;

  int RPC_Hello_Execute(const databaseRPC::HelloRequest &request, databaseRPC::HelloReply *reply)
  {
    ClientContext context;
    Status status = stub_->Hello(&context, request, reply);
    return status.error_code();
  }
};

int main()
{
  std::cout << "webserviced started" << std::endl;

  //create a gRPC stub and connect to gRPC channel without SSL
  //CreateCustomChannel adds more configuration options
  ClientDbRPC rpc_client_db(grpc::CreateChannel("localhost:50051", grpc::InsecureChannelCredentials()));

  //call Hello RPC
  rpc_client_db.RPC_Hello();


  return 0;
}

/************************************ FUNCTION DEFINITIONS ***************************************/
