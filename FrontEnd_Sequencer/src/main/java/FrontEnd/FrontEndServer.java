package FrontEnd;

import javax.xml.ws.Endpoint;

public class FrontEndServer {
    public static void main(String[] args) {
        ServerImplementation server = new ServerImplementation();

        String URL = "http://localhost:" + 9000 + "/";
        Endpoint endpoint = Endpoint.publish(URL, server);
        System.out.println("Web service server running: " + endpoint.isPublished());
    }
}