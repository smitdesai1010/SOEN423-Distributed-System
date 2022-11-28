package FrontEnd;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.json.simple.JSONObject;

public class Client {
    public static void main(String[] args) throws UnknownHostException {
        String serverURL = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + 9000 + "/?wsdl";

        try {
            URL url = new URL(serverURL);
            QName qName = new QName("http://FrontEnd/", "ServerImplementationService");
            Service service = Service.create(url, qName);
            ServerInterface server = service.getPort(ServerInterface.class);

            JSONObject obj = new JSONObject();

            obj.put("MethodName","addReservationSlot");
            obj.put("adminID", "MTLA0000");
            obj.put("eventType", "ArtGallery");
            obj.put("eventID", "MTLM011022");
            obj.put("capacity", 7);

            String resp = server.executeRequest(obj.toJSONString());
            System.out.println(resp);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}