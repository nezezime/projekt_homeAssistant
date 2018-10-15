#ifndef DATA_BASE_H
#define DATA_BASE_H

#include <algorithm>
#include <chrono>
#include <iostream>
#include <stdio.h>
#include <string>
#include <unistd.h>

#include <grpc/grpc.h>
#include <grpcpp/server.h>
#include <grpcpp/server_builder.h>
#include <grpcpp/server_context.h>
#include <grpcpp/security/server_credentials.h>

//includi zbuildane kode -> TODO popravi pot
#include "../storage.grpc.pb.h"

namespace database
{
  void RunServer(void);
}

#endif
