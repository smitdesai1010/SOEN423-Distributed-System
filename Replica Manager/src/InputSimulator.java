import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;

public class InputSimulator {
    private static final int FRONT_END_PORT = 2304;

    public static void main(String args[]) throws IOException {

        (new ListeningThread()).start();

        JSONObject samplePayload = new JSONObject();
        samplePayload.put("MethodName", "reserveTicket");
        samplePayload.put("participantID", "MTLP0000");
        samplePayload.put("eventType", "ArtGallery");
        samplePayload.put("eventID", "MTLA111022");
        samplePayload.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload.put(jsonFieldNames.SEQUENCE_NUMBER, 0);

        multicast(samplePayload);
    }

    // note: stolen & modified from the sequencer just for my personal testing purposes
    private static boolean multicast(JSONObject obj) throws IOException {
        MulticastSocket mSocket = new MulticastSocket();
        InetAddress group = InetAddress.getByName(ReplicaManager.GROUP_ADDRESS);
        DatagramPacket packet = new DatagramPacket(obj.toJSONString().getBytes(), obj.toJSONString().length(), group, ReplicaManager.REPLICA_MANAGER_PORT);
        try {
            mSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static class ListeningThread extends Thread {
        @Override
        public void run() {
            DatagramSocket udpSocket = null;
            try {
                udpSocket = new DatagramSocket(FRONT_END_PORT);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }

            while (true) {
                DatagramPacket frontEndRequestPacket = new DatagramPacket(new byte[1000], 1000);
                // Receive a request using the packet we just created
                try {
                    udpSocket.receive(frontEndRequestPacket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String frontEndRequestString = new String(frontEndRequestPacket.getData(), 0, frontEndRequestPacket.getLength());
                JSONParser jsonParser = new JSONParser();
                JSONObject frontEndRequestObject ;
                try {
                    frontEndRequestObject = (JSONObject) jsonParser.parse(frontEndRequestString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(frontEndRequestObject.toJSONString());
            }
        }
    }

}
