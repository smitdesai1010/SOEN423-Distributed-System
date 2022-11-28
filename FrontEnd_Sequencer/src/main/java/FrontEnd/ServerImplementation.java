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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "FrontEnd.ServerInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ServerImplementation implements ServerInterface {

    // Note: SOAP spins up a new thread automatically to handle mulitple concurrent requests
    // Note: Timeouts will be handled by the RM
    // Note: On Software failure: Multicast a message to all the RM informing them about the faultly RM
    //       The faulty RM will unregister itself from the group and kill itself

    // TODO: Race conditions?
    public static int ALIVE_REPLICAS = 3;

    @Override
    public String executeRequest(JSONObject requestData) {

        System.out.println("Received request: " + requestData.toString());

        try {
            // By passing in 0, the system automatically picks a free port
            DatagramSocket aSocket = new DatagramSocket(0);
            requestData.put("frontend-IP", InetAddress.getLocalHost().getHostAddress());
            requestData.put("port", aSocket.getLocalPort());

            new Sequencer().multicast(requestData);
            JSONObject[] responseData = listenForResponse(aSocket);

            return checkForByzantineFailureAndReturnTheConsensusResponse(responseData);
        }
        catch (Exception e) {
            System.out.println("Exception in Frontend: " + e.getMessage());
            return "Internal Server Error";
        }
    }

    private String checkForByzantineFailureAndReturnTheConsensusResponse(JSONObject[] responseData) throws IOException {
        int totalSuccess = 0;
        String response = "Interal Server Error";

        // If there are only 2 alive replicas, we cannot check for failure using majority election
        if (ALIVE_REPLICAS == 2) {
            return responseData[0].get("Data").toString();
        }

        // Find the majority answer
        for (JSONObject obj : responseData) {
            totalSuccess += Boolean.parseBoolean(obj.get("Success").toString()) ? 1 : -1;
        }

        for (JSONObject obj : responseData) {
            int objSucessValue = Boolean.parseBoolean(obj.get("Success").toString()) ? 1 : -1;

            // If the success value of the obj is opposite of the total value, then it has produced a wrong resut
            if (objSucessValue * totalSuccess < 0) {
                JSONObject errorMessageObj = new JSONObject();
                errorMessageObj.put("MethodName", "killReplica");
                errorMessageObj.put("FailedReplicaIP", obj.get("IP").toString());

                // TODO: Will the sequence numbers cause any errors here?
                // Multicast the message and faulty RM will deactivate itself
                // The frontEnd doesn't wait for a ACK response from teh faulty RM
                new Sequencer().multicast(errorMessageObj);
                ALIVE_REPLICAS = 2;
            }

            else {
                response = obj.get("Data").toString();
            }
        }

        return response;
    }

    private JSONObject[] listenForResponse(DatagramSocket aSocket) throws IOException, ParseException {
        List<JSONObject> responseData = new ArrayList<>();
        JSONParser parser = new JSONParser();
        System.out.println("Starting UDP Server to wait for responses");

        while (responseData.size() < ALIVE_REPLICAS) {
            byte[] requestByteArray = new byte[50000];
            DatagramPacket request = new DatagramPacket(requestByteArray, requestByteArray.length);
            aSocket.receive(request);

            String requestString = new String(request.getData()).trim();
            JSONObject requestObject = (JSONObject) parser.parse(requestString);

            responseData.add(requestObject);
        }

        aSocket.close();
        return responseData.toArray(new JSONObject[0]);
    }
}

