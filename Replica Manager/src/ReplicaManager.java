import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class ReplicaManager {
    final static int REPLICA_MANAGER_PORT = 3435; // todo: I want to change this to 2300
    final static int MONTREAL_REPLICA_PORT = 2301;
    final static int TORONTO_REPLICA_PORT = 2302;
    final static int VANCOUVER_REPLICA_PORT = 2303;
    final static String GROUP_ADDRESS = "255.1.2.3";

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        if (args.length != 1)  {
           System.out.println("Please provide a replica implementation number");
           System.out.println("Usage: replica_manager [replica_implementation_num]");
           return;
        }

        int replicaImplementationNum;
        try {
            replicaImplementationNum = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("Implementation number could not be formatted as an integer");
            return;
        }

        if (replicaImplementationNum <= 0 || replicaImplementationNum >= 4) {
            System.out.println("Please select an implementation number between 1-3");
            return;
        }

        // spawning a replica for each city
        Process montrealReplica = createReplica("Montreal", replicaImplementationNum);
        Process torontoReplica = createReplica("Toronto", replicaImplementationNum);
        Process vancouverReplica = createReplica("Vancouver", replicaImplementationNum);

        // NOTE: sometimes you need to wait a bit after creating the replicas before sending requests
        //Thread.sleep(1000);

        /*
        JSONObject samplePayload = new JSONObject();
        samplePayload.put("MethodName", "reserveTicket");
        samplePayload.put("participantID", "MTLP0000");
        samplePayload.put("eventType", "ArtGallery");
        samplePayload.put("eventID", "MTLA111022");

        JSONObject response = handleFrontEndObject(samplePayload);
        System.out.println(response.toJSONString());
         */

        // sending and receiving requests
        InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
        MulticastSocket udpSocket = new MulticastSocket(REPLICA_MANAGER_PORT);
        udpSocket.joinGroup(group);

        while (true) {
            // Create a packet for the client request
            DatagramPacket frontEndRequestPacket = new DatagramPacket(new byte[1000], 1000);
            // Receive a request using the packet we just created
            udpSocket.receive(frontEndRequestPacket);
            String frontEndRequestString = new String(frontEndRequestPacket.getData(), 0, frontEndRequestPacket.getLength());
            JSONParser jsonParser = new JSONParser();
            JSONObject frontEndRequestObject = (JSONObject) jsonParser.parse(frontEndRequestString);

            JSONObject replyObject = handleFrontEndObject(frontEndRequestObject);
            final byte[] replyObjectData = replyObject.toJSONString().getBytes();

            // Create a packet for the reply
            DatagramPacket serverReplyPacket = new DatagramPacket(replyObjectData, replyObjectData.length, frontEndRequestPacket.getAddress(), frontEndRequestPacket.getPort());
            // Send the reply
            udpSocket.send(serverReplyPacket);

            break;
        }

        System.out.println("all done");
    }

    private static JSONObject sendMessageToLocalHost(int port, JSONObject jsonObject) throws IOException, ParseException {
        // converting input JSONObject into bytes
        final byte[] data = jsonObject.toJSONString().getBytes();

        DatagramSocket udpSocket = new DatagramSocket();
        InetAddress udpClientHost = InetAddress.getByName("localhost");
        // creating the client request packet using the clientMessage string
        DatagramPacket clientRequestPacket = new DatagramPacket(data, data.length, udpClientHost, port);
        udpSocket.send(clientRequestPacket);
        // creating a reply packet to hold the reply from the server
        DatagramPacket serverReplyPacket = new DatagramPacket(new byte[1000], 1000);
        // receiving the reply packet from the server
        udpSocket.receive(serverReplyPacket);

        // converting reply bytes to a JSONObject
        String jsonString = new String(serverReplyPacket.getData(), 0, serverReplyPacket.getLength());
        JSONParser jsonParser = new JSONParser();
        JSONObject replyObject = (JSONObject) jsonParser.parse(jsonString);
        return replyObject;
    }

    private static Process createReplica(String cityName, int implementationNumber) {
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "replica.jar",  cityName);
        pb.redirectErrorStream(true);
        File log = new File("logs/" + cityName + ".log");
        pb.redirectOutput(log);
        File f = new File("replica_implementations/impl" + String.valueOf(implementationNumber));
        pb.directory(f);
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return  process;
    }

    private static JSONObject handleFrontEndObject(JSONObject frontEndObject) throws IOException, ParseException {
        String cityPrefix;

        if (frontEndObject.containsKey(jsonFieldNames.ADMIN_ID)) {
            cityPrefix = ((String) frontEndObject.get(jsonFieldNames.ADMIN_ID)).substring(0, 3);
        } else if (frontEndObject.containsKey(jsonFieldNames.PARTICIPANT_ID)) {
            cityPrefix = ((String) frontEndObject.get(jsonFieldNames.PARTICIPANT_ID)).substring(0, 3);
        } else {
            System.out.println("There is no participant or admin ID. Failed to identify which server to send the request to");
            return null;
        }

        int cityPort;
        switch (cityPrefix) {
            case "MTL":
                cityPort = MONTREAL_REPLICA_PORT;
                break;
            case "TOR":
                cityPort = TORONTO_REPLICA_PORT;
                break;
            case "VAN":
                cityPort = VANCOUVER_REPLICA_PORT;
                break;
            default:
                System.out.println("Something went wrong parsing the city prefix");
                return null;
        }

        return sendMessageToLocalHost(cityPort, frontEndObject);
    }
}
