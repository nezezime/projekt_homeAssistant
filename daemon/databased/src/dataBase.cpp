#include "dataBase.h"

/************************************* FUNCTION PROTOTYPES ***************************************/


using grpc::Server;
using grpc::ServerContext;
using grpc::ServerReader;
using grpc::ServerReaderWriter;
using grpc::ServerWriter;
using grpc::Status;
using databaseRPC::dbRPC;

//using namespace std;
using namespace ::sql;



/************************************** CLASS DEFINITIONS *****************************************/

/**
 * Implements all the service methods described in storage.proto by inheriting the Service class
 * @note: RPC methods are called from multiple threads at the same, the implementation must be
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
};

int main(int argc, char **argv)
{
  std::cout << "databased started" << std::endl;

  //connect to database

/* x DevAPI
  try
  {
    mysqlx::Session mySession(mysqlx::SessionOption::HOST, "192.168.1.2",
                              //mysqlx::SessionOption::PORT, 3306,
                              mysqlx::SessionOption::USER, "username",
                              mysqlx::SessionOption::PWD, "password",
                              mysqlx::SessionOption::DB, "home_assistant"
                              );
  }
  catch(const ::mysqlx::Error &mysql_error)
  {
    std::cout << "mysql error caught: " << mysql_error << std::endl;
    exit(1);
  }
*/

  //JDBC API
  try
  {
    sql::Driver * driver;
    sql::Connection *con;
    sql::Statement *stmt;
    sql::ResultSet *res;

    /* Create a connection */
    driver = sql::mysql::get_mysql_driver_instance();
    con = driver->connect("tcp://192.168.1.2:3306", "username", "password");
    /* Connect to the MySQL test database */
    con->setSchema("home_assistant");

    stmt = con->createStatement();
    res = stmt->executeQuery("SELECT * FROM users");
    while(res->next())
    {
      std::cout << "\t... MySQL replies: ";
      /* Access column data by alias or column name */
      std::cout << res->getString("username") << std::endl;
      std::cout << "\t... MySQL says it again: ";
      /* Access column data by numeric offset, 1 is the first column */
      std::cout << res->getString(1) << std::endl;
    }
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
