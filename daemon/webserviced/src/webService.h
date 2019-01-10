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
constexpr int WS_ERROR_AUTHENTICATION = -4;

constexpr char WS_ERROR_GENERAL_TEXT[] = "WS_ERROR_GENERAL";
constexpr char WS_ERROR_AUTHENTICATION_TEXT[] = "WS_ERROR_AUTHENTICATION";

//other constants
constexpr unsigned int DEFAULT_SESSION_TIMEOUT = 100000;

namespace SoapResponse
{
  int fillGetUsers(struct soap *soap,
                  const databaseRPC::GetUsersResponse &rpc_response,
                  ha__GetUsersResponse &gsoap_response);

  int fillUserLoginResponse(struct soap *soap,
                      const databaseRPC::UserLoginResponse &rpc_response,
                      ha__UserLoginResponse &gsoap_response);

  int fillGetMessagesResponse(struct soap *soap,
                              const databaseRPC::GetMessagesResponse &rpc_response,
                              ha__GetMessagesResponse &gsoap_response);
}

//helper functions
namespace WsHelp
{
  //checks for forbidden characters in a string to prevent sql injection
  // @param input: input string to be checked
  // @param forbidden: forbidden character detected
  int checkForbiddenChar(const std::string &input, char &forbidden);
}

//represents an active session
class SessionContainer
{
  public:
  int session_id;
  unsigned int timeout;

  SessionContainer(int id, unsigned int timeout)
  {
    this->session_id = id;
    this->timeout = timeout;
  }
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
      std::cout << "SessionManager session found" << std::endl;
      return WS_OK;
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
      return WS_OK;
    }
  }

  //terminate session (log out)
  int terminateSession(int key)
  {
    if(active_sessions.erase(key) == 1)
    {
      std::cout << "SessionManager session " << std::to_string(key) << " terminated" << std::endl;
      return WS_OK;
    }
    else
    {
      std::cout << "SessionManager terminate session error" << std::endl;
      return -1;
    }
  }

  //check if session is active and user authorized
  int authCheck(int key)
  {
    if(searchSession(key) != WS_OK)
    {
      return WS_ERROR_AUTHENTICATION;
    }
    return WS_OK;
  }

  //check for timeouts
  //TODO use std::this_thread::sleep_until for periodic execution in separate thread
  int timeouts()
  {
    return WS_OK;
  }

  int size()
  {
    return active_sessions.size();
  }
};

extern SessionManager session_manager;

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
