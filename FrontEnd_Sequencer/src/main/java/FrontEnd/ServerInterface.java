package FrontEnd;

import org.json.simple.JSONObject;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ServerInterface {
    String executeRequest(JSONObject obj);
}


