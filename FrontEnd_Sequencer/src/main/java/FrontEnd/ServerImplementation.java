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
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "FrontEnd.ServerInterface")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ServerImplementation implements ServerInterface {

    public final int SOCKET_TIMEOUT = 7000;
    public final int TOTAL_RM       = 3;
    // Note: SOAP spins up a new thread automatically to handle mulitple concurrent requests
    @Override
    public String executeRequest(String jsonString) {

        try {
            Logger logger = new Logger();
            JSONParser parser = new JSONParser();
            JSONObject requestData = (JSONObject) parser.parse(jsonString);

            logger.addToLogs("\nReceived request");

            // By passing in 0, the system automatically picks a free port
            DatagramSocket aSocket = new DatagramSocket(0);
            requestData.put("frontend-IP", InetAddress.getLocalHost().getHostAddress());
            requestData.put("port", aSocket.getLocalPort());

            new Sequencer().multicast(requestData, logger);
            JSONObject[] responseData = listenForResponse(aSocket, logger);

            return checkForByzantineFailureAndReturnTheConsensusResponse(responseData, logger);
        }
        catch (Exception e) {
            System.out.println("Exception in Frontend: " + e.getMessage());
            return "Internal Server Error";
        }
    }

    private String checkForByzantineFailureAndReturnTheConsensusResponse(JSONObject[] responseData, Logger logger) throws IOException {
        int totalSuccess = 0;
        String response = "Internal Server Error";
        logger.addToLogs("Checking for Byzantine Failure");

        // Find the majority answer
        for (JSONObject obj : responseData) {
            totalSuccess += Boolean.parseBoolean(obj.get("Success").toString()) ? 1 : -1;
        }

        for (JSONObject obj : responseData) {
            int objSucessValue = Boolean.parseBoolean(obj.get("Success").toString()) ? 1 : -1;

            // If the success value of the obj is opposite of the total value, then it has produced a wrong resut
            if (objSucessValue * totalSuccess < 0) {
                JSONObject errorMessageObj = new JSONObject();
                errorMessageObj.put("MethodName", "restartReplicas");

                logger.addToLogs("Byzantine Failure Found at Replica: " + obj.get("IP").toString());
                new Sequencer().multicast(errorMessageObj, logger);
            }

            else {
                response = obj.get("Data").toString();
            }
        }

        logger.addToLogs("Sending back the response to the Client: " + response);
        logger.flush();
        return response;
    }

    private JSONObject[] listenForResponse(DatagramSocket aSocket, Logger logger) throws IOException, ParseException {
        List<JSONObject> responseData = new ArrayList<>();
        JSONParser parser = new JSONParser();
        logger.addToLogs("Starting UDP Server to wait for responses");

        aSocket.setSoTimeout(SOCKET_TIMEOUT);

        while (responseData.size() < TOTAL_RM) {
            try {
                byte[] requestByteArray = new byte[50000];
                DatagramPacket request = new DatagramPacket(requestByteArray, requestByteArray.length);
                aSocket.receive(request);

                String requestString = new String(request.getData()).trim();
                JSONObject requestObject = (JSONObject) parser.parse(requestString);

                logger.addToLogs("Received Response from RM - "+ (responseData.size() + 1) +": " + requestObject.toString());
                responseData.add(requestObject);
            }

            catch (SocketTimeoutException e) {
                logger.addToLogs("Timeout occured while waiting for responses");
                break;
            }
        }

        aSocket.close();
        return responseData.toArray(new JSONObject[0]);
    }
}

