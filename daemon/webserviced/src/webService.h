#ifndef WEB_SERVICE_H
#define WEB_SERVICE_H

#include <algorithm>
#include <chrono>
#include <iostream>
#include <memory>
#include <pthread.h>
#include <random>
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
constexpr unsigned int soap_server_port = 26000;
constexpr unsigned int soap_send_timeout = 10; //seconds
constexpr unsigned int soap_recv_timeout = 10; //seconds
constexpr unsigned int soap_accept_timeout = 0; //seconds
constexpr unsigned int soap_max_keep_alive = 100;
constexpr unsigned int soap_max_req_backlog = 10;

#endif
