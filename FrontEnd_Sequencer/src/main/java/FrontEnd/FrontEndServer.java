package FrontEnd;

import javax.xml.ws.Endpoint;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class FrontEndServer {
    public static void main(String[] args) throws UnknownHostException {
        ServerImplementation server = new ServerImplementation();

        String URL = "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + 9000 + "/";
        Endpoint endpoint = Endpoint.publish(URL, server);
        System.out.println("Web service server running: " + endpoint.isPublished());
        System.out.println("URL: " + URL);
    }
}