#ifndef DATA_BASE_H
#define DATA_BASE_H

#include <algorithm>
#include <chrono>
#include <fstream>
#include <iostream>
#include <sstream>
#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <unistd.h>
#include <vector>

//grpc
#include <grpc/grpc.h>
#include <grpcpp/server.h>
#include <grpcpp/server_builder.h>
#include <grpcpp/server_context.h>
#include <grpcpp/security/server_credentials.h>

#include "storage.grpc.pb.h"

//mysql connector
//#include <mysqlx/xdevapi.h> //x DevAPI

//JDBC
#include <jdbc/mysql_driver.h>
#include <jdbc/mysql_connection.h>

//TODO optimize includes
/*#include <jdbc/cppconn/build_config.h>
#include <jdbc/cppconn/config.h>
#include <jdbc/cppconn/connection.h>
#include <jdbc/cppconn/datatype.h>
#include <jdbc/cppconn/metadata.h>
#include <jdbc/cppconn/parameter_metadata.h>
#include <jdbc/cppconn/prepared_statement.h>
#include <jdbc/cppconn/resultset_metadata.h>
#include <jdbc/cppconn/sqlstring.h>
#include <jdbc/cppconn/variant.h>
#include <jdbc/cppconn/version_info.h>
#include <jdbc/cppconn/warning.h>*/
#include <jdbc/cppconn/driver.h>
#include <jdbc/cppconn/exception.h>
#include <jdbc/cppconn/resultset.h>
#include <jdbc/cppconn/statement.h>

//global constants
constexpr char conf_file_name[] = "database.conf";

namespace database
{
  void RunServer(void);
}

//automatically deallocates the sql statement and result on destruction
class AutoSqlStmt
{
  public:
  sql::Statement *stmt;
  sql::ResultSet *result;

  void executeQuery(const std::string query)
  {
    result = stmt->executeQuery(query);
  }

  AutoSqlStmt(sql::Connection *sql_connection)
  {
    stmt = sql_connection->createStatement();
  };

  ~AutoSqlStmt()
  {
    std::cout << "Sql statement and result deleted" << std::endl;
    delete stmt;
    delete result;
  };
};

#endif
