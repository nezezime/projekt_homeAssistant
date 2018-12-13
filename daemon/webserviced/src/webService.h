#ifndef WEB_SERVICE_H
#define WEB_SERVICE_H

#include <algorithm>
#include <chrono>
#include <ctime>
#include <iostream>
#include <iomanip>
#include <iterator>
#include <map>
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
  int fillGetUsers(struct soap *soap,
                  const databaseRPC::GetUsersResponse &rpc_response,
                  ha__GetUsersResponse &gsoap_response);

  int fillUserResponse(struct soap *soap,
                      const databaseRPC::UserLoginResponse &rpc_response,
                      ha__UserLoginResponse &gsoap_response);
}

//represents an active session
class SessionContainer
{
  public:
  int session_id;
  unsigned int timeout;
};

class SessionManager
{
  private:
  std::map<int, SessionContainer> active_sessions;

  public:
  //find session by key
  int searchSession(int key)
  {
    if(active_sessions.find(key) == active_sessions.end())
    {
      std::cout << "SessionManager session not found" << std::endl;
      return -1;
    }
    else
    {
      return 0;
    }
  }

  //add a new session
  int addSession(SessionContainer session)
  {
    if(active_sessions.insert(std::pair<int, SessionContainer> (session.session_id, session)).second == false)
    {
      std::cout << "SessionManager failed to insert sesion" << std::endl;
      return -1;
    }
    else
    {
      return 0;
    }
  }

  //check for timeouts
  //TODO use std::this_thread::sleep_until for periodic execution in separate thread
  int timeouts()
  {
    return 0;
  }

  int size()
  {
    return active_sessions.size();
  }
};

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
