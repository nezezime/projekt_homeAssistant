# homeAssistant

Three main components:
 - SOAP web service
 - android application
 - web app

### Web service
Written in C++, using the gSOAP library: https://www.genivia.com/downloads.html
Currently includes 2 separate components, webserviced and database. The former is multithreaded and handles SOAP requests, whilst the latter performs MariaDB database communication using C++ JDBC API (https://dev.mysql.com/doc/connector-cpp/8.0/en/connector-cpp-introduction.html). Inter process communication is performed using gRPC (https://grpc.io/) and google protocol buffers (https://developers.google.com/protocol-buffers/) for message serialization. Proto 2 syntax is used.

### Web application
There is a NodeJS server which employs a strong-soap module which allows it to act as a SOAP client and a regular web server to the web application. The application allows the users to each have their own session. User credentials are shared between web and android application. Currently supports only messaging.

### Android application
Users log into the application using their credentials. Besides messaging, the application allows the user to view weather forecast (geolocation can be used) and the arrival times of LPP (Ljubljana public transport) busses. Ksoap 2 library is used as to provide a SOAP client.

### Installation
- install the gSOAP library, mysql C++ JDBC connector, gRPC and protocol buffers. The links are provided above, all the libraries have been successfully installed on a Fedora Workstation 28 system
- WSDL file which is used to specify the SOAP webservices is located in lib/wsdl. Should the gSOAP installation be successful webserviced can be compiled using the *make* in the webserviced folder
- install NodeJS on the server. Additional node modules can be installed using the NPM (node package manager). Use the *npm install + packagename* command:
  - strong-soap
  - express
  - client-sessions
  - body-parser
 - make sure that firewall is configured to allow traffic to the NodeJS server (usually port 80)
 - *apk* for android application is available here:  https://drive.google.com/open?id=19sqqVIfllgRsH93IadPtntBb0FuTOE8l
