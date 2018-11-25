#include "webService.h"

int SoapResponse::fillGetUsers(struct soap *soap,
                              const databaseRPC::GetUsersResponse &rpc_response,
                              ha__GetUsersResponse &gsoap_response)
{
  int rpc_response_size = rpc_response.user_info_size();
  std::cout << "fillGetUsers rpc response size " << rpc_response_size << std::endl;

  if(rpc_response_size == 0)
  {
    std::cout << "WARNING fillGetUsers: empty RPC response" << std::endl;
  }

  for(int i=0; i<rpc_response_size; i++)
  {
    __ha__GetUsersResponse_sequence *user_response = soap_new___ha__GetUsersResponse_sequence(soap);
    ::databaseRPC::GetUsersResponse_UserInfoBlock rpc_user_info = rpc_response.user_info(i);

    user_response->user_name = rpc_user_info.user_name();
    user_response->user_id = rpc_user_info.user_id();
    gsoap_response.__GetUsersResponse_sequence.push_back(*user_response);
  }

  return WS_OK;
}

int SoapResponse::fillUserResponse(struct soap *soap,
                                  const databaseRPC::UserLoginResponse &rpc_response,
                                  ha__UserLoginResponse &gsoap_response)
{
  std::cout << "fillUserResponse" << std::endl;

  return WS_OK;
}
