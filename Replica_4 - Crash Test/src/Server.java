import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Server {
    final static int MONTREAL_REPLICA_PORT = 2301;
    final static int TORONTO_REPLICA_PORT = 2302;
    final static int VANCOUVER_REPLICA_PORT = 2303;

    enum jsonFieldNames {
        Success,

        Data;

        String key;

        jsonFieldNames() {
            this.key = this.name();
        }
    }

    public static void main(String[] args) {
        int port;

        switch (args[0]) {
            case "Montreal":
                port = MONTREAL_REPLICA_PORT;
                break;
            case "Toronto":
                port = TORONTO_REPLICA_PORT;
                break;
            case "Vancouver":
                port = VANCOUVER_REPLICA_PORT;
                break;
            default:
                System.out.println("Incorrect City Name Provided in Arguments");
                return;
        }

        DatagramSocket socket;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.out.println("UDP Exception");
            e.printStackTrace();
            return;
        }

        System.out.println("Starting server: " + args[0] + " - Corrupted Test");

        while (true) {
            try {
                DatagramPacket clientRequestPacket = new DatagramPacket(new byte[1000], 1000);
                socket.receive(clientRequestPacket);
                System.out.println("received client packet");
                String clientRequestString = new String(clientRequestPacket.getData(), 0,
                        clientRequestPacket.getLength());
                JSONParser jsonParser = new JSONParser();
                JSONObject clientRequestObject = (JSONObject) jsonParser.parse(clientRequestString);

                JSONObject response = new JSONObject();

                response = new JSONObject();
                response.put(jsonFieldNames.Success.key, false);
                response.put(jsonFieldNames.Data.key, "Error - corrupted replica");

                final byte[] replyObjectData = response.toJSONString().getBytes();

                // Create a packet for the reply
                DatagramPacket serverReplyPacket = new DatagramPacket(replyObjectData, replyObjectData.length, clientRequestPacket.getAddress(), clientRequestPacket.getPort());
                // Send the reply
                socket.send(serverReplyPacket);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

    }
}
