var express = require('express');
var app = express();
var router = express.Router();
var fs = require('fs');
var soap = require('strong-soap').soap;
var bodyParser = require('body-parser');
var session = require('client-sessions');
app.use(bodyParser.urlencoded({ extended: true }));

//soap client init
var wsdlPath = "../lib/wsdl/ws_home_assistant_1.wsdl";
var soapOptions = {endpoint: "http://192.168.1.15:26000"};
var soapSessionId = -1;
var soapUserId = 4;
var service = 'HAServices';
var binding = 'HASOAP';
var tmpGlobal = 0;

console.log("Loading wsdl from file: " + wsdlPath);
var soapClient;
soap.createClient(wsdlPath, soapOptions, function(err, client) {
  soapClient = client;
});

//__dirname points to current working directory
var path = __dirname + '/views/';
console.log("path to views: " + path);

////////////// HELPER FUNCTIONS ///////////////////////////
function readHtmlFile(filename) {
  data = fs.readFileSync(filename, 'utf8');
  return data;
};

// todo from-time parameter is optional
function soapGetMessages(sessionId, userId) {
  return new Promise(function(resolve, reject) {
    var soapMethod = soapClient[service][binding]['GetMessages'];
    var paramsGetMessages = {
      GetMessagesRequest: {
        'session-id': sessionId,
        'user-id': userId
      }
    };

    soapMethod(paramsGetMessages, function(err, result, envelope, soapHeader) {
      //console.log(JSON.stringify(result));
      resolve(result.message);
    });
  });
}

function soapUserLogin(userName, userPassword) {
  return new Promise(function(resolve, reject) {
    var soapMethod = soapClient[service][binding]['UserLogin'];
    var paramsUserLogin = {
      UserLoginRequest: {
        'user-name': userName,
        'password': userPassword
      }
    };

    soapMethod(paramsUserLogin, function(err, result, envelope, soapHeader) {
      console.log(result);
      resolve(result);
    });
  });
}

//definition of Router middle layer
//this is executed before any other route
router.use(function (req, res, next) {
  console.log("/" + req.method);
  //allows the Router to get executed
  next();
});

//TODO make this a login page
router.get("/", function(req, res){
  res.sendFile(path + "index.html");
});

router.post("/check", function(req, res, next) {
  console.log(req.body);

  //perform login logic
  soapUserLogin(req.body.username, req.body.password)
  .then(function(result) {
    if(parseInt(result['status-code']) == 0) {
      soapSessionId = result['session-id'];
      console.log("LOGIN SUCCESS");

      //return session cookie

      res.redirect("/messages");
    } else {
      console.log("UserLogin error");
      res.redirect("/");
    }
  })
  .catch(function() {
    console.log("UserLogin error");
    res.redirect("/");
  });
});

router.get("/new_message", function(req, res){
  res.sendFile(path + "new_message.html");
});

router.get("/test", function(req, res) {
  var htmlData = readHtmlFile(path + "test.html");
  res.write(htmlData);

  //make a soap request
  var clientDscr = soapClient.describe();
  console.log(clientDscr);
  var soapMethod = soapClient[service][binding]['GetDateTime'];
  var paramsDateTimeGet = {GetDateTimeRequest: ''};

  soapMethod(paramsDateTimeGet, function(err, result, envelope, soapHeader) {
    var soapResponse = JSON.stringify(result);
    console.log(soapResponse);
    res.write('<div class="jumbotron"><p>Server Time: ');
    res.write(soapResponse);
    htmlData = readHtmlFile(path + "tmp.html");
    res.end(htmlData);
  });
});

router.get("/messages", function(req, res) {
  var htmlData = readHtmlFile(path + "messages.html");
  res.write(htmlData);

  //fetch messages using soap
  soapGetMessages(soapSessionId, soapUserId)
  .then(function(result) {
    //parse response and generate html
    console.log(result);
    //console.log(typeof(result));
    tmpGlobal = tmpGlobal + 1;
    for(var val in result) {
      message = result[val];
      res.write('<div class="row"><div class="col-sm-2"><p>');
      var date = new Date(message['message-timestamp']);
      var minutes = '0' + date.getMinutes();
      var seconds = '0' + date.getSeconds();
      res.write(date.getHours() + ':' + minutes.substr(-2) + ':' + seconds.substr(-2));

      res.write('</p></div>');
      res.write('<div class="col-sm-2"><p>');
      res.write(message['author-name'] + tmpGlobal);
      res.write('</p></div>');

      res.write('<div class="col-sm-8"><p>');
      res.write(message['message-content']);
      res.write('</p></div></div>');
    }
    res.end('</div></body></html>');
  })
  .catch(function() {
    console.log("GetMessages error");
  });
});

//this tells the app to use the above defined routes
app.use("/", router);

//this is the last possible route that can be executed
//i.e. there are no other routes - this is a 404 error
app.use("*",function(req, res){
  res.sendFile(path + "404.html");
});

app.listen(80,function(){
  console.log("Live at Port 80");
});
