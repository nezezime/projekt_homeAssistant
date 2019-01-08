var express = require('express');
var app = express();
var router = express.Router();
var fs = require('fs');
var soap = require('strong-soap').soap;

//soap client init
var wsdlPath = "../lib/wsdl/ws_home_assistant_1.wsdl";
var soapOptions = {endpoint: "http://192.168.1.15:26000"};
var service = 'HAServices';
var binding = 'HASOAP';

console.log("Loading wsdl from file: " + wsdlPath);
var soapClient;
soap.createClient(wsdlPath, soapOptions, function(err, client) {
  soapClient = client;
});

//__dirname points to current working directory
var path = __dirname + '/views/';
console.log("path to views: " + path);

//definition of Router middle layer
//this is executed before any other route
router.use(function (req,res,next) {
  console.log("/" + req.method);
  //allows the Router to get executed
  next();
});

router.get("/", function(req, res){
  res.sendFile(path + "index.html");
});

router.get("/new_message", function(req, res){
  res.sendFile(path + "new_message.html");
});

function readHtmlFile(filename) {
  data = fs.readFileSync(filename, 'utf8');
  return data;
};

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

  /*fs.readFile(path + "test.html", 'utf8', function(err, data) {
    if(err) {
      throw err;
    }
    res.write(data);



    fs.readFile(path + "tmp.html", 'utf8', function(err, data) {
      if(err) {
        throw err;
      }
      res.end(data);
    });
  });
*/
});

//this tells the app to use the above defined routes
app.use("/",router);

//this is the last possible route that can be executed
//i.e. there are no other routes - this is a 404 error
app.use("*",function(req, res){
  res.sendFile(path + "404.html");
});

app.listen(80,function(){
  console.log("Live at Port 80");
});
