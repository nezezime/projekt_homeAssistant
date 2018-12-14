#include "dataBase.h"

using grpc::Server;
using grpc::ServerContext;
using grpc::ServerReader;
using grpc::ServerReaderWriter;
using grpc::ServerWriter;
using grpc::Status;
using databaseRPC::dbRPC;

//using namespace std;
using namespace ::sql;

//global variables
sql::Driver *sql_driver;
sql::Connection *sql_connection;

/************************************** CLASS DEFINITIONS *****************************************/

/**
 * Implements all the service methods described in storage.proto by inheriting the Service class
 * @note: RPC methods are called from multiple threads at the same time, the implementation must be
 * thread safe!
 */
class dbRPCImpl final : public dbRPC::Service
{
public:
  //RPC Hello
  Status Hello(ServerContext* context,
              const ::databaseRPC::HelloRequest* request,
              ::databaseRPC::HelloReply* response) override
  {
    if(request->has_message_id() == false)
    {
      std::cout << "Hello RPC request missing message_id" << std::endl;
      return Status(grpc::StatusCode::FAILED_PRECONDITION, "missing message_id");
    }

    int message_id = request->message_id();
    std::cout << "RPC server Hello id " << message_id << std::endl;
    response->set_message_id(1234);
    response->set_message("Server Hello");
    return Status::OK;
  }

  //RPC GetUsers
  Status GetUsers(::grpc::ServerContext* context,
                const ::databaseRPC::GetUsersRequest* request,
                ::databaseRPC::GetUsersResponse* response) override
  {
    if(request->has_session_id() == false)
    {
      std::cout << "GetUsers RPC request missing message_id" << std::endl;
      return Status(grpc::StatusCode::FAILED_PRECONDITION, "missing message_id");
    }

    try
    {
      //read list of users from database
      AutoSqlStmt auto_sql(sql_connection);
      auto_sql.executeQuery("SELECT * FROM users");

      while(auto_sql.result->next())
      {
        ::databaseRPC::GetUsersResponse_UserInfoBlock *user_info_block = response->add_user_info();
        user_info_block->set_user_name(auto_sql.result->getString("username"));
        user_info_block->set_user_id(auto_sql.result->getUInt("uid"));
        std::cout << auto_sql.result->getString("username") << std::endl;
        std::cout << auto_sql.result->getUInt("uid") << std::endl;
      }
    }
    catch(sql::SQLException &e)
    {
      std::cout << "MySQL error code: " << e.getErrorCode() << std::endl;
      return Status(grpc::StatusCode::INTERNAL, "database access error");
    }

    return Status::OK;
  }

  //RPC UserLogin
  Status UserLogin(::grpc::ServerContext* context,
                    const ::databaseRPC::UserLoginRequest* request,
                    ::databaseRPC::UserLoginResponse* response) override
  {
    if(!request->has_user_name() || !request->has_password())
    {
      std::cout << "UserLogin RPC request missing user_name or password" << std::endl;
      return Status(grpc::StatusCode::FAILED_PRECONDITION, "missing user_name or password");
    }

    try
    {
      AutoSqlStmt auto_sql(sql_connection);
      std::string sql_query = "SELECT COUNT(*) FROM users WHERE username = '" + request->user_name() +
                              "' AND password ='" + request->password() + "'";
      auto_sql.executeQuery(sql_query);

      // check if only a single entry matches user name and password
      if(auto_sql.result->next())
      {
        if(auto_sql.result->getUInt("COUNT(*)") == 1)
        {
          std::cout << "Login success" << std::endl;
          response->set_status_code(0);

          //get next session id from database
          sql_query = "SELECT MAX(session_id) FROM users";
          auto_sql.executeQuery(sql_query);
          if(auto_sql.result->next())
          {
            int session_id = auto_sql.result->getUInt("MAX(session_id)") + 1;
            response->set_session_id(session_id);

            //store assigned sesion id
            sql_query = "UPDATE users SET session_id = " + std::to_string(session_id) +
                        " WHERE username = '" + request->user_name() + "'";
            std::cout << sql_query << std::endl;
            std::cout << "assigned session id " << session_id << " to user "
                      << request->user_name() << std::endl;

            //TODO this raises an exception even though the statement is fine and
            //does what it was supposed to
            auto_sql.executeQuery(sql_query);
          }
        }
        else
        {
          std::cout << "Login failed: invalid username or password" << std::endl;
          response->set_status_code(-1);
          response->set_reason("invalid username or password");
          return Status::OK;
        }
      }
    }
    catch(sql::SQLException &e)
    {
      int error_code = e.getErrorCode();
      if(error_code == 0)
      {
        std::cout << "exception raised with status code 0" << std::endl;
        return Status::OK;
      }

      std::cout << "MySQL error code: " << error_code << std::endl;
      return Status(grpc::StatusCode::INTERNAL, "database access error");
    }

    return Status::OK;
  }

  //RPC GetMessages
  Status GetMessages(::grpc::ServerContext* context,
                    const ::databaseRPC::GetMessagesRequest* request,
                    ::databaseRPC::GetMessagesResponse* response) override
  {
    if(request->has_from_time())
    {
      time_t from_time = static_cast<time_t> (request->from_time());
      std::cout << "GetMessages " << std::to_string(from_time) << std::endl;
    }

    try
    {
      AutoSqlStmt auto_sql(sql_connection);
      std::string sql_query = "";

      //TODO read database

      auto_sql.executeQuery(sql_query);
    }
    catch(sql::SQLException &e)
    {
      int error_code = e.getErrorCode();
      if(error_code == 0)
      {
        std::cout << "exception raised with status code 0" << std::endl;
        return Status::OK;
      }

      std::cout << "MySQL error code: " << error_code << std::endl;
      return Status(grpc::StatusCode::INTERNAL, "database access error");
    }

    return Status::OK;
  }

};

int main(int argc, char **argv)
{
  std::cout << "databased started" << std::endl;
  std::string db_user = "";
  std::string db_password = "";

  //parse the config file
  std::ifstream conf_file(conf_file_name);
  if(conf_file.is_open())
  {
    std::cout << "Reading config file" << std::endl;
    std::string line;

    while(getline(conf_file, line))
    {
      //whitespace removal
      line.erase(std::remove_if(line.begin(), line.end(), isspace), line.end());

      if(line[0] == '#' || line.empty())
      {
        continue;
      }

      //split the line at the delimiter
      int delimiterPos = line.find("=");
      std::string tag = line.substr(0, delimiterPos);

      if(tag.compare("user") == 0)
      {
        db_user = line.substr(delimiterPos + 1);

      }
      else if(tag.compare("password") == 0)
      {
        db_password = line.substr(delimiterPos + 1);
      }
      else
      {
        std::cout << "config file invalid argument" << std::endl;
        return -1;
      }
    }
  }
  else
  {
    std::cout << "Could not open config file" << std::endl;
    return -1;
  }
  std::cout << "u: " << db_user << " p: " << db_password << std::endl;

  //connect to database using jdbc API
  try
  {
    sql_driver = sql::mysql::get_mysql_driver_instance();
    sql_connection = sql_driver->connect("tcp://192.168.1.2:3306", db_user, db_password);
    sql_connection->setSchema("home_assistant");
    std::cout << "Connected to database" << std::endl;

/*    stmt = sql_connection->createStatement();
    res = stmt->executeQuery("SELECT * FROM users");
    while(res->next())
    {
      std::cout << "\t... MySQL replies: ";
      //Access column data by alias or column name
      std::cout << res->getString("username") << std::endl;
      //Access column data by numeric offset, 1 is the first column
      std::cout << res->getString(1) << std::endl;
    }

    delete res;
    delete stmt;
*/
    //nekatere fje niso thread safe !
  }
  catch(sql::SQLException &e)
  {
    std::cout << "# ERR: SQLException in " << __FILE__;
    std::cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << std::endl;
    std::cout << "# ERR: " << e.what();
    std::cout << " (MySQL error code: " << e.getErrorCode();
    std::cout << ", SQLState: " << e.getSQLState() << " )" << std::endl;
  }

  database::RunServer();
  delete sql_connection;
  return 0;
}

/************************************ FUNCTION DEFINITIONS ***************************************/
void database::RunServer(void)
{
  std::string server_address("0.0.0.0:50051");
  std::cout << "Starting databased RPC server" << std::endl;

  //instantaniation of RPC service implementation class
  //TODO v njihovem primeru so parsali se nek db file -> preveri, ce pride v postev zate
  dbRPCImpl service;

  grpc::ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
  builder.RegisterService(&service);
  std::unique_ptr<Server> server(builder.BuildAndStart());
  std::cout << "Server listening on " << server_address << std::endl;

  //blocking wait until the process is killed
  server->Wait();
}
