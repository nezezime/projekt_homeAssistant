package si.uni_lj.fe.tnuv.projekt_tnuv.soap_webservices;
import org.ksoap2.serialization.PropertyInfo;
import java.util.ArrayList;
import java.util.List;

public class SoapRequest {

    public SoapRequest() {
        this.parameters = new ArrayList<>();
    }

    private String soapMethod;
    private String soapAddress;
    public List<PropertyInfo> parameters;

    //getters and setters
    public String getSoapMethod() {
        return soapMethod;
    }

    public void setSoapMethod(String soapMethod) {
        this.soapMethod = soapMethod;
    }

    public String getSoapAddress() {
        return soapAddress;
    }

    public void setSoapAddress(String soapAddress) {
        this.soapAddress = soapAddress;
    }
}
