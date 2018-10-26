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
    sql::Driver * driver;
    sql::Connection *con;
    sql::Statement *stmt;
    sql::ResultSet *res;

    driver = sql::mysql::get_mysql_driver_instance();
    con = driver->connect("tcp://192.168.1.2:3306", db_user, db_password);
    con->setSchema("home_assistant");

    stmt = con->createStatement();
    res = stmt->executeQuery("SELECT * FROM users");
    while(res->next())
    {
      std::cout << "\t... MySQL replies: ";
      /* Access column data by alias or column name */
      std::cout << res->getString("username") << std::endl;
      /* Access column data by numeric offset, 1 is the first column */
      std::cout << res->getString(1) << std::endl;
    }

    //ne pozabi dealokacije, nekatere fje niso thread safe
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
