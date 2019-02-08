package si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import si.uni_lj.fe.tnuv.projekt_tnuv.MessageManagement.messageStorageManager;
import si.uni_lj.fe.tnuv.projekt_tnuv.data.messageTable;
import si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices.response_classes.GetUsersResponse;

public class SoapWebserviceManager {

    private messageStorageManager storageManager;

    public SoapWebserviceManager(Context context) {
        storageManager = new messageStorageManager(context);
    }

    //SOAP session info
    private static int sessionId = -1;
    public static boolean userLoggedIn = false;
    public static int userId = -1;
    private static String userName = "";
    private static String userPasswd = "";
    static int timeout = 5000;

    //shared SOAP request parameters
    //private static final String SOAP_ADDRESS = "http://192.168.1.15:26000/";
    private static final String SOAP_ADDRESS = "http://193.77.126.31:26000/";
    //private static final String URL = "http://192.168.1.15:26000";
    private static final String URL = "http://193.77.126.31:26000";
    private static final String NAMESPACE = "http://home-assistant.namespace/ha/";

    //request-specific parameters
    private static final String SOAP_ADDRESS_GetDateTime = SOAP_ADDRESS + "Geuser-idtDateTime";
    private static final String SOAP_METHOD_GetDateTime = "GetDateTimeRequest";

    private static final String SOAP_ADDRESS_GetUsers = SOAP_ADDRESS + "GetUsers";
    private static final String SOAP_METHOD_GetUsers = "GetUsersRequest";

    private static final String SOAP_ADDRESS_UserLogin = SOAP_ADDRESS + "UserLogin";
    private static final String SOAP_METHOD_UserLogin = "UserLoginRequest";

    private static final String SOAP_ADDRESS_UserLogout = SOAP_ADDRESS + "UserLogout";
    private static final String SOAP_METHOD_UserLogout = "UserLogoutRequest";

    private static final String SOAP_ADDRESS_GetMessages = SOAP_ADDRESS + "GetMessages";
    private static final String SOAP_METHOD_GetMessages = "GetMessagesRequest";

    private static final String SOAP_ADDRESS_PostMessage = SOAP_ADDRESS + "PostMessage";
    private static final String SOAP_METHOD_PostMessage = "PostMessageRequest";

    private static class soapRequestAsync extends AsyncTask<SoapRequest, Void, Object> {

        @Override
        protected Object doInBackground(SoapRequest... soapRequests) {

            SoapRequest request = soapRequests[0];
            Log.d("SOAP", "doInBackground size: " + request.parameters.size());
            SoapObject soapRequest = new SoapObject(NAMESPACE, request.getSoapMethod());

            //add property infos to request
            int parametersSize = request.parameters.size();
            for(int i=0; i<parametersSize; i++) {
                soapRequest.addProperty(request.parameters.get(i));
            }

            //serialize the request
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
            envelope.implicitTypes = true;
            envelope.setOutputSoapObject(soapRequest);

            //transport the request
            HttpTransportSE httpTransport = new HttpTransportSE(URL);
            httpTransport.debug = true;

            try {
                httpTransport.call(request.getSoapAddress(), envelope);
            } catch (HttpResponseException e) {
                Log.e("JURE", e.getMessage());
                //e.printStackTrace();
            } catch (IOException e) {
                Log.e("JURE", e.getMessage());
                //e.printStackTrace();
            } catch (XmlPullParserException e) {
                Log.e("JURE", e.getMessage());
                //e.printStackTrace();
            }

            //parse SOAP webservice response
            Object result = null;
            //SoapPrimitive soap_result = null;
            //SoapObject result_object = null;

            result = (Object) envelope.bodyIn;
            String result_data;
            try {
                result_data = result.toString();
            } catch (NullPointerException e) {
                return null;
            }

            Log.d("SOAP", result_data);

                /*try {
                    //result = (Object )envelope.getResponse();
                    //soap_result = (SoapPrimitive) envelope.getResponse();
                    //envelope.getResponse();
                    Log.d("JURE", "result retrieved");
                    //Log.d("JURE", String.valueOf(result)); // see output in the console
                } catch (SoapFault e) {
                    Log.e("SOAPLOG", e.getMessage());
                    //e.printStackTrace();
                }
                */
            return result;
        }
    }

    public String GetDateTime() {
        Log.d("SOAP", "GetDateTime");
        String datetime = "";

        SoapRequest requestParams = new SoapRequest();
        requestParams.setSoapAddress(SOAP_ADDRESS_GetDateTime);
        requestParams.setSoapMethod(SOAP_METHOD_GetDateTime);

        soapRequestAsync soapRequestTask = new soapRequestAsync();

        try {
            Object response = soapRequestTask.execute(requestParams).get(timeout, TimeUnit.MILLISECONDS);
            datetime = response.toString();
        } catch (ExecutionException e) {
            Log.e("SOAP", "Execution exception GetDateTime");
        } catch (InterruptedException e) {
            Log.e("SOAP", "Interrupted exception GetDateTime");
        } catch (TimeoutException e) {
            Log.e("SOAP", "Timeout exception GetDateTime");
        } catch (NullPointerException e) {
            Log.e("SOAP", "NullPointer exception GetDateTime. Server is unreachable");
        }

        return datetime;
    }

    public int GetUsers() {
        Log.d("SOAP", "GetUsers");

        SoapObject response = new SoapObject();
        SoapRequest requestParams = new SoapRequest();
        requestParams.setSoapAddress(SOAP_ADDRESS_GetUsers);
        requestParams.setSoapMethod(SOAP_METHOD_GetUsers);

        //prepare request inner xml
        PropertyInfo req_sessionId = new PropertyInfo();
        req_sessionId.setName("session-id");
        req_sessionId.setValue(Integer.toString(sessionId));
        req_sessionId.setType(String.class);
        requestParams.parameters.add(req_sessionId);

        //assemble and send the request
        soapRequestAsync soapRequestTask = new soapRequestAsync();

        try {
            response = (SoapObject) soapRequestTask.execute(requestParams).get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("SOAP", "Execution exception GetUsers");
        } catch (InterruptedException e) {
            Log.e("SOAP", "Interrupted exception GetUsers");
        } catch (TimeoutException e) {
            Log.e("SOAP", "Timeout exception GetUsers");
        }

        //parse response
        if(response == null) {
            Log.w("SOAP", "GetUsers response null");
            return -1;
        }

        int responseSize = response.getPropertyCount();
        List<String> usernames = new ArrayList<>();
        List<Integer> uids = new ArrayList<>();

        //parse soap response
        for(int i=0; i<responseSize; i++) {
            String elementName = response.getPropertyInfo(i).getName();

            if(elementName.equals("user-id")) {
                int userId = Integer.parseInt(response.getProperty(i).toString());
                //Log.d("SOAP", "user-id: " + Integer.toString(userId));
                uids.add(userId);
            }
            else if(elementName.equals("user-name")) {
                //Log.d("SOAP", "user-name: " + response.getPropertyAsString(i));
                String userName = (String) response.getProperty(i).toString();
                //Log.d("SOAP", "user-name: " + userName);
                usernames.add(userName);
            }
            else {
                Log.w("SOAP", "GetUsers unexpected element type in response");
            }

        }

        if(usernames.size() != uids.size()) {
            Log.e("SOAP", "GetUsers error uids and usernames size mismatch");
            return -1;
        }

        //update users table
        //TODO this only updates existing users - new users should be added
        for(int i=0; i<usernames.size(); i++) {
            storageManager.updateUserDatabase(storageManager.setAllUserRowValues(usernames.get(i),
                                                                                0xFF0000FF,
                                                                                uids.get(i)));
        }

        //TODO this is required on fresh app installation
        storageManager.readUserDatabase();
        //storageManager.writeMessageDatabase(storageManager.setMessageRowValues(uids.get(0), "Welcome to home assistant"));
        storageManager.readMessageDatabase();

        return 0;
    }

    public int UserLogin(String name, String password) {
        Log.d("SOAP", "UserLogin");

        SoapObject response = new SoapObject(NAMESPACE, SOAP_METHOD_UserLogin);
        SoapRequest requestParams = new SoapRequest();
        requestParams.setSoapAddress(SOAP_ADDRESS_UserLogin);
        requestParams.setSoapMethod(SOAP_METHOD_UserLogin);

        //prepare request inner xml
        PropertyInfo req_userName = new PropertyInfo();
        req_userName.setNamespace("http://home-assistant.namespace/ha/");
        req_userName.setName("user-name");
        req_userName.setValue(name);
        req_userName.setType(String.class);
        requestParams.parameters.add(req_userName);

        PropertyInfo req_userPassword = new PropertyInfo();
        req_userPassword.setNamespace("http://home-assistant.namespace/ha/");
        req_userPassword.setName("password");
        req_userPassword.setValue(password);
        req_userPassword.setType(String.class);
        requestParams.parameters.add(req_userPassword);

        //assemble and send the request
        soapRequestAsync soapRequestTask = new soapRequestAsync();

        try {
            response = (SoapObject) soapRequestTask.execute(requestParams).get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("SOAP", "Execution exception UserLogin");
        } catch (InterruptedException e) {
            Log.e("SOAP", "Interrupted exception UserLogin");
        } catch (TimeoutException e) {
            Log.e("SOAP", "Timeout exception UserLogin");
        }

        //parse response
        if(response == null) {
            Log.w("SOAP", "UserLogin response null");
            return -1;
        }

        int responseSize = response.getPropertyCount();
        int statusCode = -1;
        int sessionId = -1;
        int userId = -1;

        //parse soap response
        for(int i=0; i<responseSize; i++) {
            String elementName = response.getPropertyInfo(i).getName();

            if(elementName.equals("status-code")) {
                statusCode = Integer.parseInt(response.getProperty(i).toString());
                Log.d("SOAP", "status-code: " + Integer.toString(statusCode));
            }
            else if(elementName.equals("session-id")) {
                sessionId = Integer.parseInt(response.getProperty(i).toString());
                Log.d("SOAP", "session-id: " + Integer.toString(sessionId));
            }
            else if(elementName.equals("user-id")) {
                userId = Integer.parseInt(response.getProperty(i).toString());
                Log.d("SOAP", "user-id: " + Integer.toString(userId));
            }
        }

        if(statusCode != 0) {
            Log.d("SOAP", "Login failed");
            return -2;
        }

        Log.d("SOAP", "Login successful");
        this.sessionId = sessionId;
        this.userId = userId;
        this.userLoggedIn = true;
        return 0;
    }

    public int UserLogout() {
        Log.d("SOAP", "UserLogout for sessionId " + Integer.toString(sessionId));

        SoapPrimitive response = new SoapPrimitive(NAMESPACE, "UserLogoutResponse", -1);
        SoapRequest requestParams = new SoapRequest();
        requestParams.setSoapAddress(SOAP_ADDRESS_UserLogout);
        requestParams.setSoapMethod(SOAP_METHOD_UserLogout);

        //prepare request inner xml
        PropertyInfo req_sessionId = new PropertyInfo();
        req_sessionId.setNamespace("http://home-assistant.namespace/ha/");
        req_sessionId.setName("session-id");
        req_sessionId.setValue(Integer.toString(sessionId));
        req_sessionId.setType(String.class);
        requestParams.parameters.add(req_sessionId);

        //assemble and send the request
        soapRequestAsync soapRequestTask = new soapRequestAsync();

        try {
            response = (SoapPrimitive) soapRequestTask.execute(requestParams).get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("SOAP", "Execution exception UserLogout");
        } catch (InterruptedException e) {
            Log.e("SOAP", "Interrupted exception UserLogout");
        } catch (TimeoutException e) {
            Log.e("SOAP", "Timeout exception UserLogout");
        }

        //parse response
        if(response == null) {
            Log.w("SOAP", "UserLogout response null");
            return -1;
        }

        //int responseSize = response.getPropertyCount();
        int responseValue = Integer.valueOf(response.toString());
        Log.d("SOAP", Integer.toString(responseValue));

        this.sessionId = -1;
        this.userId = -1;
        this.userLoggedIn = false;
        return 0;
    }

    public int GetMessages() {
        Log.d("SOAP", "GetMessages");

        SoapObject response = new SoapObject();
        SoapRequest requestParams = new SoapRequest();
        requestParams.setSoapAddress(SOAP_ADDRESS_GetMessages);
        requestParams.setSoapMethod(SOAP_METHOD_GetMessages);

        //get the timestamp of last received message
        long latestTimestamp = storageManager.getMostRecentMessageTimestamp();
        Log.d("SOAP", "most recent message timestamp: " + Long.toString(latestTimestamp));

        //prepare request inner xml
        PropertyInfo req_sessionId = new PropertyInfo();
        req_sessionId.setNamespace("http://home-assistant.namespace/ha/");
        req_sessionId.setName("session-id");
        req_sessionId.setValue(Integer.toString(sessionId));
        req_sessionId.setType(String.class);
        requestParams.parameters.add(req_sessionId);

        PropertyInfo req_userId = new PropertyInfo();
        req_userId.setNamespace("http://home-assistant.namespace/ha/");
        req_userId.setName("user-id");
        req_userId.setValue(Integer.toString(userId));
        req_userId.setType(String.class);
        requestParams.parameters.add(req_userId);

        PropertyInfo req_fromTimestamp = new PropertyInfo();
        req_fromTimestamp.setNamespace("http://home-assistant.namespace/ha/");
        req_fromTimestamp.setName("from-time");
        req_fromTimestamp.setValue(Long.toString(latestTimestamp));
        req_fromTimestamp.setType(String.class);
        requestParams.parameters.add(req_fromTimestamp);

        //assemble and send the request
        soapRequestAsync soapRequestTask = new soapRequestAsync();

        try {
            response = (SoapObject) soapRequestTask.execute(requestParams).get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("SOAP", "Execution exception GetMessages");
        } catch (InterruptedException e) {
            Log.e("SOAP", "Interrupted exception GetMessages");
        } catch (TimeoutException e) {
            Log.e("SOAP", "Timeout exception GetMessages");
        }

        //parse response
        if(response == null) {
            Log.w("SOAP", "GetMessages response null");
            return -1;
        }

        int responseSize = response.getPropertyCount();
        List<messageTable> messagesDb = new ArrayList<messageTable>();
        Log.d("SOAP", "GetMessages response size: " + Integer.toString(responseSize));

        //parse soap response
        for(int i=0; i<responseSize; i++) {
            String elementName = response.getPropertyInfo(i).getName();

            //message
            if(elementName.equals("message")) {
                SoapObject message = (SoapObject) response.getProperty(i);
                int messageSize = message.getPropertyCount();
                //Log.d("SOAP", "extracted message with size " + Integer.toString(messageSize));

                long messageTimestamp = 0;
                int messageAuthorUid = 0;
                String messageContent = "";

                //all parameters are required
                for(int j=0; j<messageSize; j++) {
                    String messageElementName = message.getPropertyInfo(j).getName();

                    if(messageElementName.equals("message-timestamp")) {
                        messageTimestamp = Long.parseLong(message.getProperty(j).toString());
                        //LocalDateTime ld = LocalDateTime.parse(messageDatetime);
                        Log.d("SOAP", "timestamp: " + Long.toString(messageTimestamp));
                    }
                    else if(messageElementName.equals("author-id")) {
                        messageAuthorUid = Integer.parseInt(message.getProperty(j).toString());
                        Log.d("SOAP", "author-id: " + Integer.toString(messageAuthorUid));
                    }
                    else if(messageElementName.equals("message-content")) {
                        messageContent = message.getProperty(j).toString();
                        Log.d("SOAP", "message-content: " + messageContent);
                    }
                }

                messageTable messageDb = storageManager.setMessageRowValuesTs(messageAuthorUid,
                                                                              messageTimestamp,
                                                                              messageContent);
                messagesDb.add(messageDb);
            }
        }

        //write messages to database
        storageManager.writeMessageDatabase(messagesDb.toArray(new messageTable[messagesDb.size()]));

        //storageManager.readMessageDatabase();
        return 0;
    }

    public int PostMessage(String messageContent) {
        Log.d("SOAP", "PostMessage");

        SoapPrimitive response = new SoapPrimitive(NAMESPACE, "UserLogoutResponse", -1);
        SoapRequest requestParams = new SoapRequest();
        requestParams.setSoapAddress(SOAP_ADDRESS_PostMessage);
        requestParams.setSoapMethod(SOAP_METHOD_PostMessage);

        //prepare request inner xml
        PropertyInfo req_sessionId = new PropertyInfo();
        req_sessionId.setNamespace("http://home-assistant.namespace/ha/");
        req_sessionId.setName("session-id");
        req_sessionId.setValue(Integer.toString(sessionId));
        req_sessionId.setType(String.class);
        requestParams.parameters.add(req_sessionId);

        PropertyInfo req_authorId = new PropertyInfo();
        req_authorId.setNamespace("http://home-assistant.namespace/ha/");
        req_authorId.setName("author-id");
        req_authorId.setValue(Integer.toString(userId));
        req_authorId.setType(String.class);
        requestParams.parameters.add(req_authorId);

        PropertyInfo req_messageContent = new PropertyInfo();
        req_messageContent.setNamespace("http://home-assistant.namespace/ha/");
        req_messageContent.setName("content");
        req_messageContent.setValue(messageContent);
        req_messageContent.setType(String.class);
        requestParams.parameters.add(req_messageContent);

        //assemble and send the request
        soapRequestAsync soapRequestTask = new soapRequestAsync();

        try {
            response = (SoapPrimitive) soapRequestTask.execute(requestParams).get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Log.e("SOAP", "Execution exception PostMessage");
        } catch (InterruptedException e) {
            Log.e("SOAP", "Interrupted exception PostMessage");
        } catch (TimeoutException e) {
            Log.e("SOAP", "Timeout exception PostMessage");
        }

        //parse response
        if(response == null) {
            Log.w("SOAP", "PostMessage response null");
            return -1;
        }

        int responseValue = Integer.valueOf(response.toString());
        Log.d("SOAP", Integer.toString(responseValue));
        if(responseValue != 0) {
            return -1;
        }

        return 0;
    }

    public int TestServerConnection() {
        String response = GetDateTime();
        if(response.isEmpty()) {
            return -1;
        }

        return 0;
    }
}
