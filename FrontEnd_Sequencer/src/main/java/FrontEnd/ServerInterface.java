package FrontEnd;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ServerInterface {
    String executeRequest(String jsonString);
}


