import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ReplicaManager {
    public final static int REPLICA_MANAGER_PORT = 3435;
    public final static int MONTREAL_REPLICA_PORT = 2301;
    public final static int TORONTO_REPLICA_PORT = 2302;
    public final static int VANCOUVER_REPLICA_PORT = 2303;
    public final static String GROUP_ADDRESS = "225.1.2.3";
    private static int nextSequenceNum = 0;
    private static HashMap<Integer, JSONObject> requestQueue;
    private static int replicaImplementationNumber;
    public static Logger logger;

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        try {
            FileHandler fh = new FileHandler("logs/replica_manager.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (args.length != 1)  {
           logger.severe("Please provide a replica implementation number");
           logger.severe("Usage: replica_manager [replica_implementation_num]");
           return;
        }

        try {
            replicaImplementationNumber = Integer.valueOf(args[0]);
        } catch (NumberFormatException e) {
            logger.severe("Implementation number could not be formatted as an integer");
            return;
        }

        if (replicaImplementationNumber <= 0 || replicaImplementationNumber >= 4) {
            logger.severe("Please select an implementation number between 1-3");
            return;
        }
        logger.info("Starting Replica Manager with implementation number [" + replicaImplementationNumber  + "] for replicas...");

        // spawning a replica for each city
        City.MONTREAL.startProcess(replicaImplementationNumber);
        City.TORONTO.startProcess(replicaImplementationNumber);
        City.VANCOUVER.startProcess(replicaImplementationNumber);

        logger.info("Joining multicast group...");
        InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
        MulticastSocket multicastSocket = new MulticastSocket(REPLICA_MANAGER_PORT);
        multicastSocket.joinGroup(group);

        int sequenceNumber = 0;
        requestQueue = new HashMap<Integer, JSONObject>();

        // sending and receiving requests
        logger.info("Listening for requests...");
        while (true) {
            // Create a packet for the client request
            DatagramPacket frontEndRequestPacket = new DatagramPacket(new byte[1000], 1000);
            // Receive a request using the packet we just created
            multicastSocket.receive(frontEndRequestPacket);

            logger.info("Received a request packet, processing it now...");

            String frontEndRequestString = new String(frontEndRequestPacket.getData(), 0, frontEndRequestPacket.getLength());
            JSONParser jsonParser = new JSONParser();
            JSONObject frontEndRequestObject = (JSONObject) jsonParser.parse(frontEndRequestString);

            handleFrontEndObject(frontEndRequestObject);
        }
    }

    public static JSONObject sendMessageToLocalHost(City city, JSONObject jsonObject) throws IOException, ParseException {
        // converting input JSONObject into bytes
        final byte[] data = jsonObject.toJSONString().getBytes();

        DatagramSocket udpSocket = new DatagramSocket();
        udpSocket.setSoTimeout(5000);
        InetAddress udpClientHost = InetAddress.getByName("localhost");
        // creating the client request packet using the clientMessage string
        DatagramPacket clientRequestPacket = new DatagramPacket(data, data.length, udpClientHost, city.replicaPort);
        udpSocket.send(clientRequestPacket);
        // creating a reply packet to hold the reply from the server
        DatagramPacket serverReplyPacket = new DatagramPacket(new byte[1000], 1000);
        // receiving the reply packet from the server
        try {
            logger.info("Communicating with the associated replica...");
            udpSocket.receive(serverReplyPacket);
        } catch (SocketTimeoutException e) {
            logger.info("Communication with replica timed out after 5 seconds...");
            city.startProcess(ReplicaManager.replicaImplementationNumber);
            return sendMessageToLocalHost(city, jsonObject);
        }

        // converting reply bytes to a JSONObject
        String jsonString = new String(serverReplyPacket.getData(), 0, serverReplyPacket.getLength());
        JSONParser jsonParser = new JSONParser();
        JSONObject replyObject = (JSONObject) jsonParser.parse(jsonString);
        return replyObject;
    }

    private static void handleFrontEndObject(JSONObject frontEndObject) throws IOException, ParseException {
        if (frontEndObject.get(jsonFieldNames.METHOD_NAME).equals("restartReplicas"))  {
            logger.info("Restarting all replicas...");
            ++nextSequenceNum;
            for (City c : City.values()) {
                c.startProcess(replicaImplementationNumber);
            }
            return;
        }

        int sequenceNumber = Math.toIntExact((long) frontEndObject.get(jsonFieldNames.SEQUENCE_NUMBER));
        if (sequenceNumber != nextSequenceNum) {
            requestQueue.put(sequenceNumber, frontEndObject);
            logger.info("Received a request with sequence number [" + sequenceNumber + "] which does not match the next" +
                    " sequence number [" + nextSequenceNum + "]...");
            logger.info("Storing it in memory...");
            return;
        }
        nextSequenceNum++;

        String cityPrefix;
        if (frontEndObject.containsKey(jsonFieldNames.ADMIN_ID)) {
            cityPrefix = ((String) frontEndObject.get(jsonFieldNames.ADMIN_ID)).substring(0, 3);
        } else if (frontEndObject.containsKey(jsonFieldNames.PARTICIPANT_ID)) {
            cityPrefix = ((String) frontEndObject.get(jsonFieldNames.PARTICIPANT_ID)).substring(0, 3);
        } else {
            logger.severe("There is no participant or admin ID. Failed to identify which server to send the request to");
            return;
        }

        City city = null;
        for (City c : City.values()) {
            if (c.prefix.equals(cityPrefix)) {
                city = c;
                break;
            }
        }
        if (city == null) {
            logger.severe("Something went wrong parsing the city prefix");
            return;
        }

        if (!city.process.isAlive()) {
            city.startProcess(replicaImplementationNumber);
        }

        JSONObject replyObject = sendMessageToLocalHost(city, frontEndObject);

        String frontendIp = (String) frontEndObject.get(jsonFieldNames.FRONTEND_IP);
        long longFrontendPort = ((long)frontEndObject.get(jsonFieldNames.FRONTEND_PORT));
        int frontendPort = Math.toIntExact(longFrontendPort);

        replyObject.put(jsonFieldNames.REPLICAMANAGER_IP, InetAddress.getLocalHost().getHostAddress());
        final byte[] replyObjectData = replyObject.toJSONString().getBytes();

        logger.info("Sending request reply to the FE...");
        // Send the reply
        DatagramPacket serverReplyPacket = new DatagramPacket(replyObjectData, replyObjectData.length, InetAddress.getAllByName(frontendIp)[0], frontendPort);
        DatagramSocket udpSocket = new DatagramSocket();
        udpSocket.send(serverReplyPacket);
        logger.info("Sent...");

        if (requestQueue.containsKey(nextSequenceNum)) {
            logger.info("Processing next sequence number from memory...");
            handleFrontEndObject(requestQueue.get(nextSequenceNum));
        }
    }
}
