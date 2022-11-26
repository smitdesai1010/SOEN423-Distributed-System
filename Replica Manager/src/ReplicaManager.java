import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;

public class ReplicaManager {
    public final static int REPLICA_MANAGER_PORT = 3435;
    public final static int MONTREAL_REPLICA_PORT = 2301;
    public final static int TORONTO_REPLICA_PORT = 2302;
    public final static int VANCOUVER_REPLICA_PORT = 2303;
    public final static String GROUP_ADDRESS = "225.1.2.3";
    private static int nextSequenceNum = 0;
    private static HashMap<Integer, JSONObject> requestQueue;

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

        // making sure processes are killed when the RM exits
        Runtime.getRuntime().addShutdownHook(new ReplicaShutdownThread(montrealReplica, torontoReplica, vancouverReplica));

        // sending and receiving requests
        InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
        MulticastSocket multicastSocket = new MulticastSocket(REPLICA_MANAGER_PORT);
        multicastSocket.joinGroup(group);

        int sequenceNumber = 0;
        requestQueue = new HashMap<Integer, JSONObject>();

        //todo: send back port number of RM

        while (true) {
            // Create a packet for the client request
            DatagramPacket frontEndRequestPacket = new DatagramPacket(new byte[1000], 1000);
            // Receive a request using the packet we just created
            multicastSocket.receive(frontEndRequestPacket);
            String frontEndRequestString = new String(frontEndRequestPacket.getData(), 0, frontEndRequestPacket.getLength());
            JSONParser jsonParser = new JSONParser();
            JSONObject frontEndRequestObject = (JSONObject) jsonParser.parse(frontEndRequestString);

            handleFrontEndObject(frontEndRequestObject);
        }
    }

    public static JSONObject sendMessageToLocalHost(int port, JSONObject jsonObject) throws IOException, ParseException {
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

    private static void handleFrontEndObject(JSONObject frontEndObject) throws IOException, ParseException {
        int sequenceNumber = Math.toIntExact((long) frontEndObject.get(jsonFieldNames.SEQUENCE_NUMBER));
        if (sequenceNumber != nextSequenceNum) {
            requestQueue.put(sequenceNumber, frontEndObject);
            return;
        }
        nextSequenceNum++;

        String cityPrefix;
        if (frontEndObject.containsKey(jsonFieldNames.ADMIN_ID)) {
            cityPrefix = ((String) frontEndObject.get(jsonFieldNames.ADMIN_ID)).substring(0, 3);
        } else if (frontEndObject.containsKey(jsonFieldNames.PARTICIPANT_ID)) {
            cityPrefix = ((String) frontEndObject.get(jsonFieldNames.PARTICIPANT_ID)).substring(0, 3);
        } else {
            System.out.println("There is no participant or admin ID. Failed to identify which server to send the request to");
            return;
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
                return;
        }

        JSONObject replyObject = sendMessageToLocalHost(cityPort, frontEndObject);

        String frontendIp = (String) frontEndObject.get(jsonFieldNames.FRONTEND_IP);
        long longFrontendPort = ((long)frontEndObject.get(jsonFieldNames.FRONTEND_PORT));
        int frontendPort = Math.toIntExact(longFrontendPort);

        replyObject.put(jsonFieldNames.REPLICAMANAGER_IP, InetAddress.getLocalHost().getHostAddress());
        final byte[] replyObjectData = replyObject.toJSONString().getBytes();

        // Send the reply
        DatagramPacket serverReplyPacket = new DatagramPacket(replyObjectData, replyObjectData.length, InetAddress.getAllByName(frontendIp)[0], frontendPort);
        DatagramSocket udpSocket = new DatagramSocket();
        udpSocket.send(serverReplyPacket);

        if (requestQueue.containsKey(nextSequenceNum))
            handleFrontEndObject(requestQueue.get(nextSequenceNum));
    }
}
