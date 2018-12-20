#include "webService.h"
#include "HASOAP.nsmap"

using grpc::Channel;
using grpc::ClientContext;
using grpc::ClientReader;
using grpc::ClientReaderWriter;
using grpc::ClientWriter;
using grpc::Status;
using databaseRPC::dbRPC;

// global variables
SessionManager session_manager;

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
    request.set_message_id(0);

    //send RPC
    //int result = RPC_Hello_Execute(request, &reply);
    int result = RPC_Request_Execute(request, &reply, &dbRPC::Stub::Hello);
    if(result != 0)
    {
      std::cout << "RPC failed with error code " << result << std::endl;
      return result;
    }

    //parse response
    std::cout << "RPC response OK for message_id " << reply.message_id() << std::endl;
    return WS_OK;
  }

  int RPC_GetUsers(struct soap *soap, int session_id, ha__GetUsersResponse &gsoap_response)
  {
    databaseRPC::GetUsersRequest request;
    databaseRPC::GetUsersResponse reply;

    request.set_session_id(session_id);
    int result = RPC_Request_Execute(request, &reply, &dbRPC::Stub::GetUsers);
    if(result != 0)
    {
      std::cout << "RPC failed with error code " << result << std::endl;
      return result;
    }

    //write results to soap response
    result = SoapResponse::fillGetUsers(soap, reply, gsoap_response);
    if(result != 0)
    {
      std::cout << "Failed to fill gSOAP response structure with error code " << result << std::endl;
      return result;
    }

    return WS_OK;
  }

  int RPC_UserLogin(struct soap *soap,
                  std::string user_name,
                  std::string password,
                  ha__UserLoginResponse &gsoap_response)
  {
    databaseRPC::UserLoginRequest request;
    databaseRPC::UserLoginResponse reply;

    request.set_user_name(user_name);
    request.set_password(password);
    int result = RPC_Request_Execute(request, &reply, &dbRPC::Stub::UserLogin);
    if(result != 0)
    {
      std::cout << "RPC failed with error code " << result << std::endl;
      return result;
    }

    //write results to soap response
    result = SoapResponse::fillUserLoginResponse(soap, reply, gsoap_response);
    if(result != 0)
    {
      std::cout << "Failed to fill gSOAP response structure with error code " << result << std::endl;
      return result;
    }

    return WS_OK;
  }

  int RPC_GetMessages(struct soap *soap,
                      int user_id,
                      time_t *from_time,
                      ha__GetMessagesResponse &gsoap_response)
  {
    databaseRPC::GetMessagesRequest request;
    databaseRPC::GetMessagesResponse reply;

    request.set_user_id(user_id);
    if(from_time != nullptr)
    {
      request.set_from_time(*from_time);
    }

    int result = RPC_Request_Execute(request, &reply, &dbRPC::Stub::GetMessages);
    if(result != 0)
    {
      std::cout << "RPC failed with error code " << result << std::endl;
      return result;
    }

    //write results to soap response
    result = SoapResponse::fillGetMessagesResponse(soap, reply, gsoap_response);
    if(result != 0)
    {
      std::cout << "Failed to fill gSOAP response structure with error code " << result << std::endl;
      return result;
    }

    return WS_OK;
  }

  int RPC_PostMessage(struct soap *soap,
                      databaseRPC::PostMessageRequest request,
                      int &gsoap_response)
  {
    databaseRPC::PostMessageResponse reply;
    int result = RPC_Request_Execute(request, &reply, &dbRPC::Stub::PostMessage);
    if(result != 0)
    {
      std::cout << "RPC failed with error code " << result << std::endl;
      return result;
    }

    gsoap_response = reply.result();
    return WS_OK;
  }

private:
  std::unique_ptr<dbRPC::Stub> stub_;
/*
  int RPC_Hello_Execute(const databaseRPC::HelloRequest &request, databaseRPC::HelloReply *reply)
  {
    ClientContext context;
    Status status = stub_->Hello(&context, request, reply);
    return status.error_code();
  }
*/

  template <typename RpcRequest, typename RpcResponse>
  int RPC_Request_Execute(const RpcRequest &request, RpcResponse *reply,
                         grpc::Status (dbRPC::Stub::*method)(ClientContext *, const RpcRequest &, RpcResponse *))
  {
    ClientContext context;
    Status status = (*stub_.*method)(&context, request, reply);
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
  struct soap *soap_safe_copy;
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
  result = rpc_client_db.RPC_GetUsers(soap, ha__GetUsersRequest_->session_id, ha__GetUsersResponse_);
  if(result != 0)
  {
    std::cout << "GetUsers RPC error" << std::endl;
    return WS_ERROR_RPC;
  }

  return WS_OK;
}

int HASOAPService::UserLogin(ha__UserLoginRequest *ha__UserLoginRequest_, ha__UserLoginResponse &ha__UserLoginResponse_)
{
  int result = 0;
  std::cout << "UserLogin" << std::endl;

  std::cout << "username: " << ha__UserLoginRequest_->user_name << std::endl;
  std::cout << "passwd: " <<  ha__UserLoginRequest_->password << std::endl;

  result = rpc_client_db.RPC_UserLogin(soap,
                                      ha__UserLoginRequest_->user_name,
                                      ha__UserLoginRequest_->password,
                                      ha__UserLoginResponse_);
  if(result != 0)
  {
    std::cout << "GetUsers RPC error" << std::endl;
    return WS_ERROR_RPC;
  }

  //TODO read data from soap in a way that prevents sql injection


  return WS_OK;
}

int HASOAPService::UserLogout(ha__UserLogoutRequest *ha__UserLogoutRequest_, int &ha__UserLogoutResponse)
{
  int result = 0;
  std::cout << "UserLogout" << std::endl;

  result = session_manager.terminateSession(ha__UserLogoutRequest_->session_id);
  if(result != 0)
  {
    std::cout << "UserLogout error" << std::endl;
    return soap_receiver_fault(soap, WS_ERROR_GENERAL_TEXT, "UserLogout error");
  }

  return WS_OK;
}

int HASOAPService::GetMessages(ha__GetMessagesRequest *ha__GetMessagesRequest_, ha__GetMessagesResponse &ha__GetMessagesResponse_)
{
  int result = 0;
  std::cout << "GetMessages" << std::endl;

  result = session_manager.authCheck(ha__GetMessagesRequest_->__GetMessagesRequest_sequence->session_id);
  if(result != WS_OK)
  {
    std::cout << "GetMessages authentication error" << std::endl;
    return soap_receiver_fault(soap, WS_ERROR_AUTHENTICATION_TEXT, "GetMessages authentication error");
  }

  //TODO get user_id from session manager if missing from request

  result = rpc_client_db.RPC_GetMessages(soap,
                                        *ha__GetMessagesRequest_->__GetMessagesRequest_sequence->user_id,
                                        ha__GetMessagesRequest_->__GetMessagesRequest_sequence->from_time,
                                        ha__GetMessagesResponse_);
  if(result != 0)
  {
    std::cout << "GetMessages RPC error" << std::endl;
    return WS_ERROR_RPC;
  }

  return WS_OK;
}

int HASOAPService::PostMessage(ha__PostMessageRequest *ha__PostMessageRequest_, int &ha__PostMessageResponse)
{
  int result = 0;
  std::cout << "PostMessage" << std::endl;

  //check authorization
  result = session_manager.authCheck(ha__PostMessageRequest_->session_id);
  if(result != WS_OK)
  {
    std::cout << "PostMessage authentication error" << std::endl;
    return soap_receiver_fault(soap, WS_ERROR_AUTHENTICATION_TEXT, "PostMessage authentication error");
  }

  //fill proto request parameters
  databaseRPC::PostMessageRequest proto_request;
  proto_request.set_session_id(ha__PostMessageRequest_->session_id);
  proto_request.set_author_id(ha__PostMessageRequest_->author_id);
  proto_request.set_message_content(ha__PostMessageRequest_->content);

  //optional parameters
  if(ha__PostMessageRequest_->author_name != nullptr)
  {
    proto_request.set_author_name(*ha__PostMessageRequest_->author_name);
  }
  if(ha__PostMessageRequest_->dst_id != nullptr)
  {
    proto_request.set_destination_id(*ha__PostMessageRequest_->dst_id);
  }
  if(ha__PostMessageRequest_->dst_name != nullptr)
  {
    proto_request.set_destination_name(*ha__PostMessageRequest_->dst_name);
  }

  result = rpc_client_db.RPC_PostMessage(soap, proto_request, ha__PostMessageResponse);
  if(result != 0)
  {
    std::cout << "PostMessage RPC error" << std::endl;
    return WS_ERROR_RPC;
  }

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
