import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;

public class InputSimulator {
    private static final int FRONT_END_PORT = 2304;

    public static void main(String args[]) throws IOException, InterruptedException {

        (new ListeningThread()).start();

        // todo: create test payloads for every operation type (will probably never do this kek)

        JSONObject samplePayload0 = new JSONObject();
        samplePayload0.put("MethodName", jsonFieldNames.RESERVE_TICKET);
        samplePayload0.put("participantID", "MTLP0000");
        samplePayload0.put("eventType", "ArtGallery");
        samplePayload0.put("eventID", "MTLA111022");
        samplePayload0.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload0.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload0.put(jsonFieldNames.SEQUENCE_NUMBER, 0);

        JSONObject samplePayload1 = new JSONObject();
        samplePayload1.put("MethodName", jsonFieldNames.RESERVE_TICKET);
        samplePayload1.put("participantID", "MTLP0000");
        samplePayload1.put("eventType", "ArtGallery");
        samplePayload1.put("eventID", "TORA011022");
        samplePayload1.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload1.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload1.put(jsonFieldNames.SEQUENCE_NUMBER, 1);

        JSONObject samplePayload2 = new JSONObject();
        samplePayload2.put("MethodName",jsonFieldNames.GET_EVENT_SCHEDULE);
        samplePayload2.put("participantID", "MTLP0000");
        samplePayload2.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload2.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload2.put(jsonFieldNames.SEQUENCE_NUMBER, 2);

        JSONObject samplePayload3 = new JSONObject();
        samplePayload3.put("MethodName",jsonFieldNames.ADD_RESERVATION_SLOT);
        samplePayload3.put("adminID", "MTLA0000");
        samplePayload3.put("eventType", "ArtGallery");
        samplePayload3.put("eventID", "MTLM011022");
        samplePayload3.put(jsonFieldNames.CAPACTIY, 7);
        samplePayload3.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload3.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload3.put(jsonFieldNames.SEQUENCE_NUMBER, 3);

        Thread.sleep(1000);
        multicast(samplePayload3);
        Thread.sleep(1000);
        multicast(samplePayload1);
        Thread.sleep(1000);
        multicast(samplePayload0);
        Thread.sleep(1000);
        multicast(samplePayload2);
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
