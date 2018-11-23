#include "webService.h"
#include "HASOAP.nsmap"

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

  int RPC_Hello(void)
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
      return result;
    }

    //parse response
    std::cout << "RPC response OK for message_id " << reply.message_id() << std::endl;
    return 0;
  }

  int RPC_GetUsers(int session_id, ha__GetUsersResponse &ha__GetUsersResponse_)
  {
    databaseRPC::GetUsersRequest request;
    databaseRPC::GetUsersResponse reply;

    request.set_session_id(session_id);
    int result = RPC_GetUsers_Execute(request, &reply);
    if(result != 0)
    {
      std::cout << "RPC failed with error code " << result << std::endl;
      return result;
    }

    //write results to soap response
    result = SoapResponse::fillGetUsers(reply, ha__GetUsersResponse_);
    if(result != 0)
    {
      std::cout << "Failed to fill gSOAP response structure with error code " << result << std::endl;
      return result;
    }

    return 0;
  }

private:
  std::unique_ptr<dbRPC::Stub> stub_;

  int RPC_Hello_Execute(const databaseRPC::HelloRequest &request, databaseRPC::HelloReply *reply)
  {
    ClientContext context;
    Status status = stub_->Hello(&context, request, reply);
    return status.error_code();
  }

  //TODO make a template function for RPC call -> check if method name to be called can be given as function argument
  int RPC_GetUsers_Execute(const databaseRPC::GetUsersRequest &request, databaseRPC::GetUsersResponse *reply)
  {
    ClientContext context;
    Status status = stub_->GetUsers(&context, request, reply);
    return status.error_code();
  }
};

void *SoapProcessRequest(void * soap)
{
  pthread_detach(pthread_self());
  //struct soap * soap_struct = static_cast<struct soap *> (soap);
  HASOAPService ha_service(static_cast<struct soap *> (soap));
  ha_service.serve();
  /*soap_destroy(soap_struct);
  soap_end(soap_struct);
  soap_done(soap_struct);*/
  ha_service.destroy();
  free(soap);
  return nullptr;
}

//global parameters
//creates a gRPC stub and connect to gRPC channel without SSL
//CreateCustomChannel adds more configuration options
ClientDbRPC rpc_client_db(grpc::CreateChannel("localhost:50051", grpc::InsecureChannelCredentials()));

int main(int argc, char **argv)
{
  std::cout << "webserviced started" << std::endl;

  //call Hello RPC
  rpc_client_db.RPC_Hello();

  //SOAP multithreaded server initialization
  struct soap soap;
  struct soap * soap_safe_copy;
  SOAP_SOCKET soap_socket, s;
  soap_init(&soap);
  //soap_imode(&soap, SOAP_XML_STRICT);
  soap.send_timeout = soap_send_timeout;
  soap.recv_timeout = soap_recv_timeout;
  //soap.accept_timeout = soap_accept_timeout; //stops SOAP server after set time of inactivity
  //soap.max_keep_alive = soap_max_keep_alive;
  pthread_t tid;

  //reuse address
  soap.bind_flags |= SO_REUSEADDR;

  //bind the socket
  soap_socket = soap_bind(&soap, nullptr, soap_server_port, soap_max_req_backlog);
  if(!soap_valid_socket(soap_socket))
  {
    std::cerr << "webserviced error invalid SOAP server socket " << soap_socket << std::endl;
    exit(1);
  }

  std::cout << "SOAP server running @"<< "TODO address:"<< soap_server_port << std::endl;

  //request loop
  for(;;)
  {
    s = soap_accept(&soap);
    if(!soap_valid_socket(s))
    {
      if(soap.errnum)
      {
        soap_print_fault(&soap, stderr);
        exit(1);
      }
      std::cout << "SOAP server timed out" << std::endl;
      break;
    }

    soap_safe_copy = soap_copy(&soap);
    if(!soap_safe_copy)
    {
      std::cout << "SOAP server soap struct safe copy failed" << std::endl;
      break;
    }

    pthread_create(&tid, nullptr, (void *(*)(void *))SoapProcessRequest, (void *)soap_safe_copy);
  }

  //detach soap struct and close socket
  soap_closesocket(soap_socket);
  soap_done(&soap);
  return 0;
}


/************************************ WEBSERVICES METHODS ****************************************/
int HASOAPService::GetDateTime(const std::string& ha__GetDateTimeRequest, std::string &ha__GetDateTimeResponse)
{
  int result = 0;
  std::cout << "GetDateTime" << std::endl;

  //fetch required data via rpc
  result = rpc_client_db.RPC_Hello();
  if(result != 0)
  {
    std::cout << "GetDateTime RPC error" << std::endl;
    return WS_ERROR_RPC;
  }

  //parse RPC response

  //fill gsoap response
  auto t = std::time(nullptr);
  auto tm = *std::localtime(&t);
  std::ostringstream oss;
  oss << std::put_time(&tm, "%d-%m-%Y %H-%M-%S");
  ha__GetDateTimeResponse.assign(oss.str());

  return WS_OK;
}

int HASOAPService::GetUsers(ha__GetUsersRequest *ha__GetUsersRequest_, ha__GetUsersResponse &ha__GetUsersResponse_)
{
  int result = 0;
  std::cout << "GetUsers" << std::endl;

  //get data via RPC and fill gsoap response structure with results
  result = rpc_client_db.RPC_GetUsers(ha__GetUsersRequest_->session_id, ha__GetUsersResponse_);
  if(result != 0)
  {
    std::cout << "GetUsers RPC error" << std::endl;
    return WS_ERROR_RPC;
  }

  return WS_OK;
}

int HASOAPService::UserLogin(ha__UserLoginRequest *ha__UserLoginRequest_, ha__UserLoginResponse &ha__UserLoginResponse_)
{
  std::cout << "UserLogin" << std::endl;

  //TODO read data from soap in a way that prevents sql injection

  return WS_OK;
}

int HASOAPService::UserLogout(ha__UserLogoutRequest *ha__UserLogoutRequest_, int &ha__UserLogoutResponse)
{
  std::cout << "UserLogout" << std::endl;

  return WS_OK;
}

int HASOAPService::GetMessages(ha__GetMessagesRequest *ha__GetMessagesRequest_, ha__GetMessagesResponse &ha__GetMessagesResponse_)
{
  std::cout << "GetMessages" << std::endl;

  return WS_OK;
}

int HASOAPService::GetApiData(_ha__GetApiDataRequest *ha__GetApiDataRequest_, _ha__GetApiDataResponse &ha__GetApiDataResponse_)
{
  std::cout << "GetApiData" << std::endl;

  return WS_OK;
}

int HASOAPService::GetApplianceData(ha__GetApplianceDataRequest *ha__GetApplianceDataRequest_, ha__GetApplianceDataResponse &ha__GetApplianceDataResponse_)
{
  std::cout << "GetApplianceData" << std::endl;

  return WS_OK;
}

int HASOAPService::GetAppliances(ha__GetAppliancesRequest *ha__GetAppliancesRequest_, ha__GetAppliancesResponse &ha__GetAppliancesResponse_)
{
  std::cout << "GetAppliances" << std::endl;

  return WS_OK;
}

