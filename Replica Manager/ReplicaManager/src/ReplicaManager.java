import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class ReplicaManager {
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        if (args.length != 4)  {
           System.out.println("Please provide ports for the RM, and all the three replicas");
           System.out.println("Usage: replica_manager [rm_port] [montreal_port] [toronto_port] [vancouver_port]");
           return;
        }

        int replicaManagerPort;
        int replicaMontrealPort;
        int replicaTorontoPort;
        int replicaVancouverPort;
        try {
            replicaManagerPort = Integer.valueOf(args[0]);
            replicaMontrealPort = Integer.valueOf(args[1]);
            replicaTorontoPort = Integer.valueOf(args[2]);
            replicaVancouverPort = Integer.valueOf(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("One of the ports could not be formatted as an integer");
            return;
        }

        // spawning a replica (montreal)
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", "replica3.jar",  "MONTREAL", String.valueOf(replicaMontrealPort));
        pb.redirectErrorStream(true);
        File log = new File("penis.log");
        pb.redirectOutput(log);
        File f = new File("ReplicaJars/replica3");
        pb.directory(f);
        Process replica3;
        try {
            replica3 = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // sending and receiving requests

        JSONObject samplePayload = new JSONObject();
        samplePayload.put("MethodName", "reserveTicket");
        samplePayload.put("participantID", "1234");
        samplePayload.put("eventType", "ArtGallery");
        samplePayload.put("eventID", "1234");
        System.out.println("sending da message");
        System.out.println(replica3.isAlive());

        sendMessageToLocalHost(replicaMontrealPort, samplePayload);
        System.out.println("all done");

        /**
        while (true) {
            // Create a packet for the client request
            DatagramPacket clientRequestPacket = new DatagramPacket(new byte[1000], 1000);
            // Receive a request using the packet we just created
            udpSocket.receive(clientRequestPacket);

            final ByteArrayInputStream bais = new ByteArrayInputStream(clientRequestPacket.getData());
            final ObjectInputStream oos = new ObjectInputStream(bais);
            Payload payload = (Payload) oos.readObject();

            String replyString = "";

        }
         **/
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
}
