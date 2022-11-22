package FrontEnd;

import Sequencer.Sequencer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

@WebService(endpointInterface = "FrontEnd.ServerInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ServerImplementation implements ServerInterface {

    @Override
    public String executeRequest(JSONObject obj) {

        //create port number dynamically
        //Do the election thing
            //If defect, inform RM
            //Timeout?
        //send back one response to client

        Sequencer seq = new Sequencer();
        boolean multicastResult = seq.multicast(obj);

        if (!multicastResult) {
            return "Internal Server error";
        }

        JSONObject[] responseData = listenForResponse(1000);


        return "";
    }

    public JSONObject[] listenForResponse(int PORT) {

        int ctr = 0;
        JSONObject[] responseData = new JSONObject[3];
        JSONParser parser = new JSONParser();
        System.out.println("Starting UDP Server to wait for responses");

        try (DatagramSocket aSocket = new DatagramSocket(PORT)) {

            while (ctr < 3) {
                byte[] requestByteArray = new byte[10000];
                DatagramPacket request = new DatagramPacket(requestByteArray, requestByteArray.length);
                aSocket.receive(request);

                String requestString = new String(request.getData()).trim();
                JSONObject requestObject = (JSONObject) parser.parse(requestString);

                //store client ip and port number

                responseData[ctr] = requestObject;
                ++ctr;
                //timeout?
                //do I need to send back response?
            }
        }
        catch (IOException | ParseException e) {
            System.out.println("Error on UDP Server " + e.getMessage());
        }

        return responseData;
    }

}

// use multicastpublisher
// total ordering

