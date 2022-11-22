package FrontEnd;

import org.json.simple.JSONObject;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService(endpointInterface = "FrontEnd.ServerInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ServerImplementation implements ServerInterface {

    @Override
    public String executeRequest(JSONObject obj) {
        //Receive request
        //Pass the request to Sequencer
        //Do the election thing
            //If defect, inform RM
            //Timeout
        //send back one response to client
        return "";
    }
}
