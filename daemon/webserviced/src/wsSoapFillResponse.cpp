#include "webService.h"

int SoapResponse::fillGetUsers(struct soap *soap,
                              const databaseRPC::GetUsersResponse &rpc_response,
                              ha__GetUsersResponse &gsoap_response)
{
  int rpc_response_size = rpc_response.user_info_size();
  std::cout << "fillGetUsers rpc response size " << rpc_response_size << std::endl;

  if(rpc_response_size == 0)
  {
    std::cout << "WARNING fillGetUsers: empty RPC response" << std::endl;
  }

  for(int i=0; i<rpc_response_size; i++)
  {
    __ha__GetUsersResponse_sequence *user_response = soap_new___ha__GetUsersResponse_sequence(soap);
    ::databaseRPC::GetUsersResponse_UserInfoBlock rpc_user_info = rpc_response.user_info(i);

    user_response->user_name = rpc_user_info.user_name();
    user_response->user_id = rpc_user_info.user_id();
    gsoap_response.__GetUsersResponse_sequence.push_back(*user_response);
  }

  return WS_OK;
}

int SoapResponse::fillUserLoginResponse(struct soap *soap,
                                  const databaseRPC::UserLoginResponse &rpc_response,
                                  ha__UserLoginResponse &gsoap_response)
{
  gsoap_response.status_code = rpc_response.status_code();
  if(rpc_response.status_code() == 0)
  {
    if(rpc_response.has_session_id())
    {
      std::cout << "fillUserResponse login successful" << std::endl;
      int *session_id = static_cast<int *> (soap_malloc(soap, sizeof(int)));
      *session_id = rpc_response.session_id();
      gsoap_response.session_id = session_id;

      //register a new active session with session manager
      SessionContainer container(*session_id, DEFAULT_SESSION_TIMEOUT);
      session_manager.addSession(container);
      //session_manager.searchSession(*session_id);
    }

    if(rpc_response.has_user_id())
    {
      int *user_id = static_cast<int *> (soap_malloc(soap, sizeof(int)));
      *user_id = rpc_response.user_id();
      gsoap_response.user_id = user_id;
    }
  }
  else
  {
    std::cout << "fillUserResponse login failed" << std::endl;
    if(rpc_response.has_reason())
    {
      std::string *reason = soap_new_std__string(soap);
      *reason = rpc_response.reason();
      gsoap_response.reason = reason;
    }
  }

  return WS_OK;
}

int SoapResponse::fillGetMessagesResponse(struct soap *soap,
                                        const databaseRPC::GetMessagesResponse &rpc_response,
                                        ha__GetMessagesResponse &gsoap_response)
{
  int message_response_size = rpc_response.message_size();
  std::cout << "fillGetMessages rpc response size " << message_response_size << std::endl;

  __ha__GetMessagesResponse_sequence *messages_sequence = soap_new___ha__GetMessagesResponse_sequence(soap);
  for(int i=0; i<message_response_size; i++)
  {
    databaseRPC::GetMessagesResponse_MessageBlock rpc_message = rpc_response.message(i);
    _ha__GetMessagesResponse_message *message = soap_new__ha__GetMessagesResponse_message(soap);

    unsigned int *message_id = static_cast<unsigned int *> (soap_malloc(soap, sizeof(unsigned int)));
    *message_id = rpc_message.message_id();
    message->message_id = message_id;

    std::string *message_content = soap_new_std__string(soap);
    *message_content = rpc_message.message_content();
    message->message_content = *message_content;

    if(rpc_message.has_author_name())
    {
      std::string *author_name = soap_new_std__string(soap);
      *author_name = rpc_message.author_name();
      message->author_name = author_name;
    }

    message->message_timestamp = static_cast<time_t> (rpc_message.message_timestamp());
    message->author_id = rpc_message.author_id();
    messages_sequence->message.push_back(*message);
  }

  gsoap_response.__GetMessagesResponse_sequence = messages_sequence;
  return WS_OK;
}
