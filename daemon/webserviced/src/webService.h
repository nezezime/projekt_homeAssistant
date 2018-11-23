#ifndef WEB_SERVICE_H
#define WEB_SERVICE_H

#include <algorithm>
#include <chrono>
#include <ctime>
#include <iostream>
#include <iomanip>
#include <memory>
#include <pthread.h>
#include <random>
#include <sstream>
#include <stdio.h>
#include <string>
#include <unistd.h>

//includes for gRPC
#include "storage.grpc.pb.h"
#include <grpc/grpc.h>
#include <grpcpp/channel.h>
#include <grpcpp/client_context.h>
#include <grpcpp/create_channel.h>
#include <grpcpp/security/credentials.h>

//includes for gSOAP
#include "soapHASOAPService.h"
#include "soapH.h"

//constants
//soap structure parameters
constexpr unsigned int soap_server_port = 26000;
constexpr unsigned int soap_send_timeout = 10;    //seconds
constexpr unsigned int soap_recv_timeout = 10;    //seconds
constexpr unsigned int soap_accept_timeout = 0;   //seconds
constexpr unsigned int soap_max_keep_alive = 100;
constexpr unsigned int soap_max_req_backlog = 10;

//error codes
constexpr int WS_OK = 0;
constexpr int WS_ERROR_GENERAL = -1;
constexpr int WS_ERROR_RPC = -2;
constexpr int WS_ERROR_INVALID_PARAMETERS = -3;

namespace SoapResponse
{
  int fillGetUsers(const databaseRPC::GetUsersResponse &rpc_response, ha__GetUsersResponse &ha__GetUsersResponse_);
}

//closes socket automatically on destruction
class AutoSocket
{
  private:
  int socket_fd;
  int *socket_fd_ptr;

  public:
  int get()
  {
    return socket_fd;
  }
  int *reset()
  {
    int *p = socket_fd_ptr;
    return p;
  }
  AutoSocket()
  {
    //init socket file descriptor to invalid value
    socket_fd = -1;
    socket_fd_ptr = &socket_fd;
  }
  ~AutoSocket()
  {
    //std::cout << "socket fd " << socket_fd << " closed in destructor" << std::endl;
    close(socket_fd);
  }
};

#endif
