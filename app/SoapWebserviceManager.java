
package si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import si.uni_lj.fe.tnuv.projekt_tnuv.MessageManagement.SoapRequest;

public class SoapWebserviceManager {

    public SoapWebserviceManager() {
        //TODO Context is often passed to constructor
        Log.d("JURE", "soapWebserviceManager constructor");
        soapRequestAsync soapReqTest = new soapRequestAsync();
        soapReqTest.execute();
    }

    //these need to be static so the async task can accesss them
    /*private static String SOAP_ACTION = "http://tempuri.org/Add";
    private static String METHOD_NAME = "Add";
    private static String NAMESPACE = "http://tempuri.org/";
    private static String URL = "http://www.dneonline.com/calculator.asmx";
    */

    private static String SOAP_ACTION = "http://192.168.1.15:26000/GetDateTime";
    private static String METHOD_NAME = "GetDateTimeRequest";
    private static String NAMESPACE = "http://home-assistant.namespace/ha/";
    private static String URL = "http://192.168.1.15:26000";

    private static class soapRequestAsync extends AsyncTask<SoapRequest, Void, Void> {

        @Override
        protected Void doInBackground(SoapRequest... soapRequests) {
            Log.d("JURE", "executing soap request");
            SoapObject soapRequest = new SoapObject(NAMESPACE, METHOD_NAME);
            // soapRequest.addProperty("intA", "1");
            // soapRequest.addProperty("intB", "2");

            //serialize the request
            //TODO use correct SOAP version
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
            envelope.implicitTypes = true;
            envelope.setOutputSoapObject(soapRequest);

            //transport the request
            HttpTransportSE httpTransport = new HttpTransportSE(URL);
            httpTransport.debug = true;

            try {
                httpTransport.call(SOAP_ACTION, envelope);
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
            //Object result = null;
            SoapPrimitive soap_result = null;
            try {
                //result = (Object )envelope.getResponse();
                envelope.getResponse();
                Log.d("JURE", "result retrieved");
                //Log.d("JURE", String.valueOf(result)); // see output in the console
            } catch (SoapFault e) {
                Log.e("SOAPLOG", e.getMessage());
                //e.printStackTrace();
            }

            return null;
        }
    }



}
