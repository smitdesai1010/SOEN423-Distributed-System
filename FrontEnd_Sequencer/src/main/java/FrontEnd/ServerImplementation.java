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
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "FrontEnd.ServerInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ServerImplementation implements ServerInterface {

    // Note: SOAP spins up a new thread automatically to handle mulitple concurrent requests

    @Override
    public String executeRequest(JSONObject requestData) {

        System.out.println("Received request: " + requestData.toString());

        try {
            // By passing in 0, the system automatically picks a free port
            DatagramSocket aSocket = new DatagramSocket(0);
            requestData.put("port", aSocket.getLocalPort());

            boolean multicastResult = new Sequencer().multicast(requestData);

            if (!multicastResult) {
                throw new Exception("Failed to multicast request");
            }

            JSONObject[] responseData = listenForResponse(aSocket);
            checkForByzantineFailure(responseData);
            return returnTheConsensusResponse(responseData);
        }
        catch (Exception e) {
            System.out.println("Exception in Frontend: " + e.getMessage());
            return "Internal Server Error";
        }
    }

    private void checkForByzantineFailure(JSONObject[] responseData) {
        int totalSuccess = 0;
        int totalEntries = responseData.length;

        // Find the majority answer
        for (JSONObject obj : responseData) {
            totalSuccess += Boolean.parseBoolean(obj.get("Success").toString()) ? 1 : -1;
        }

        // If the value of totalSuccess is not equal to totalEntries, means one replica has sent different
        if (Math.abs(totalSuccess) != totalEntries) {
            for (JSONObject obj : responseData) {
                int objSucessValue = Boolean.parseBoolean(obj.get("Success").toString()) ? 1 : -1;

                if (objSucessValue * totalSuccess < 0) {
                    //TODO: Inform the defective replica
                }
            }
        }
    }

    private String returnTheConsensusResponse(JSONObject[] responseData) {
        int success = 0;

        for (JSONObject obj : responseData) {
            // If success is true, add 1. If success is false, add -1
            success += Boolean.parseBoolean(obj.get("Success").toString()) ? 1 : -1;

            //TODO: What if Total entries is 2 (one crashed cause of timeout)
            if (success == 2 || success == -2) {
                return obj.get("Data").toString();
            }
        }

        return "Internal Server Error";
    }

    private JSONObject[] listenForResponse(DatagramSocket aSocket) {

        List<JSONObject> responseData = new ArrayList<>();
        JSONParser parser = new JSONParser();
        System.out.println("Starting UDP Server to wait for responses");

        try {
            while (responseData.size() < 3) {
                byte[] requestByteArray = new byte[10000];
                DatagramPacket request = new DatagramPacket(requestByteArray, requestByteArray.length);
                aSocket.receive(request);

                String requestString = new String(request.getData()).trim();
                JSONObject requestObject = (JSONObject) parser.parse(requestString);

                responseData.add(requestObject);
                //TODO: Store client ip and port number in the JSON
                //TODO: Check for Timeout?
                //TODO: Do I need to send back response to RM that I received the data?
            }
        }
        catch (IOException | ParseException e) {
            System.out.println("Error on UDP Server " + e.getMessage());
        }

        return responseData.toArray(new JSONObject[0]);
    }


    public void sendUDPMessage(String IP, int PORT, JSONObject JSONData) {

        try (DatagramSocket aSocket = new DatagramSocket()) {
            byte[] byteData = JSONData.toJSONString().getBytes(StandardCharsets.UTF_8);

            InetAddress aHost = InetAddress.getByName(IP);
            DatagramPacket request = new DatagramPacket(byteData, byteData.length, aHost, PORT);
            aSocket.send(request);

            //TODO: Wait for receiving the response?
        }
        catch (IOException e) {
            System.out.println("Exception in UDP-Client: " + e.getMessage());
        }
    }
}

// what message to send on failure or crash(timeout) detection

