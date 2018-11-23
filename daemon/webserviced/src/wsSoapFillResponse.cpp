#include "webService.h"

int SoapResponse::fillGetUsers(const databaseRPC::GetUsersResponse &rpc_response, ha__GetUsersResponse &ha__GetUsersResponse_)
{
  int rpc_response_size = rpc_response.user_info_size();
  std::cout << "fillGetUsers rpc resp size " << rpc_response_size << std::endl;

  if(rpc_response_size == 0)
  {
    std::cout << "WARNING fillGetUsers: empty RPC response" << std::endl;
  }

  return WS_OK;
}