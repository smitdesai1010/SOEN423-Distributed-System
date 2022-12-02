import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.*;

public class InputSimulator {
    private static final int FRONT_END_PORT = 2304;

    public static void main(String args[]) throws IOException, InterruptedException {
        (new ListeningThread()).start();

        // Test Case 1
        JSONObject samplePayload0 = new JSONObject();
        samplePayload0.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.LIST_RESERVATION_SLOT_AVAILABLE);
        samplePayload0.put(jsonFieldNames.ADMIN_ID, "MTLA1234");
        samplePayload0.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload0.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload0.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload0.put(jsonFieldNames.SEQUENCE_NUMBER, 0);

        // Test Case 2
        JSONObject samplePayload1 = new JSONObject();
        samplePayload1.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.ADD_RESERVATION_SLOT);
        samplePayload1.put(jsonFieldNames.ADMIN_ID, "MTLA1234");
        samplePayload1.put(jsonFieldNames.EVENT_ID, "MTLNewEvent");
        samplePayload1.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload1.put(jsonFieldNames.CAPACTIY, 55);
        samplePayload1.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload1.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload1.put(jsonFieldNames.SEQUENCE_NUMBER, 1);

        // Test Case 3
        JSONObject samplePayload2 = new JSONObject();
        samplePayload2.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.ADD_RESERVATION_SLOT);
        samplePayload2.put(jsonFieldNames.ADMIN_ID, "MTLA1234");
        samplePayload2.put(jsonFieldNames.EVENT_ID, "MTLNewEvent");
        samplePayload2.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload2.put(jsonFieldNames.CAPACTIY, 55);
        samplePayload2.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload2.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload2.put(jsonFieldNames.SEQUENCE_NUMBER, 2);

        // Test Case 4
        JSONObject samplePayload3 = new JSONObject();
        samplePayload3.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.RESERVE_TICKET);
        samplePayload3.put(jsonFieldNames.PARTICIPANT_ID, "MTLP0001");
        samplePayload3.put(jsonFieldNames.EVENT_ID, "MTLE121212");
        samplePayload3.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload3.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload3.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload3.put(jsonFieldNames.SEQUENCE_NUMBER, 3);

        // Test Case 5
        JSONObject samplePayload4 = new JSONObject();
        samplePayload4.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.RESERVE_TICKET);
        samplePayload4.put(jsonFieldNames.PARTICIPANT_ID, "MTLP0001");
        samplePayload4.put(jsonFieldNames.EVENT_ID, "MTLE121212");
        samplePayload4.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload4.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload4.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload4.put(jsonFieldNames.SEQUENCE_NUMBER, 4);

        // Test Case 6
        JSONObject samplePayload5 = new JSONObject();
        samplePayload5.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.CANCEL_TICKET);
        samplePayload5.put(jsonFieldNames.PARTICIPANT_ID, "MTLP0001");
        samplePayload5.put(jsonFieldNames.EVENT_ID, "MTLE121212");
        samplePayload5.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload5.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload5.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload5.put(jsonFieldNames.SEQUENCE_NUMBER, 5);

        // Test Case 7
        JSONObject samplePayload6 = new JSONObject();
        samplePayload6.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.CANCEL_TICKET);
        samplePayload6.put(jsonFieldNames.PARTICIPANT_ID, "MTLP5555");
        samplePayload6.put(jsonFieldNames.EVENT_ID, "MTLE999999");
        samplePayload6.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload6.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload6.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload6.put(jsonFieldNames.SEQUENCE_NUMBER, 6);

        // Test Case 8
        JSONObject samplePayload7 = new JSONObject();
        samplePayload7.put(jsonFieldNames.METHOD_NAME, jsonFieldNames.EXCHANGE_TICKET);
        samplePayload7.put(jsonFieldNames.PARTICIPANT_ID, "MTLP5555");
        samplePayload7.put(jsonFieldNames.EVENT_ID, "MTLA010123");
        samplePayload7.put(jsonFieldNames.EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload7.put(jsonFieldNames.NEW_EVENT_ID, "MTLE010122");
        samplePayload7.put(jsonFieldNames.NEW_EVENT_TYPE, jsonFieldNames.ART_GALLERY);
        samplePayload7.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        samplePayload7.put(jsonFieldNames.FRONTEND_IP, "localhost");
        samplePayload7.put(jsonFieldNames.SEQUENCE_NUMBER, 7);

        // Crash Payload
        JSONObject crashPayload = new JSONObject();
        crashPayload.put("MethodName", "die");
        crashPayload.put("participantID", "MTLP0000");
        crashPayload.put(jsonFieldNames.FRONTEND_PORT, FRONT_END_PORT);
        crashPayload.put(jsonFieldNames.FRONTEND_IP, "localhost");
        crashPayload.put(jsonFieldNames.SEQUENCE_NUMBER, 0);

        // Software Failure Payload
        JSONObject softwareFailurePayload = new JSONObject();
        softwareFailurePayload.put("MethodName", "restartReplicas");

        multicast(samplePayload0);
        multicast(samplePayload1);
        multicast(samplePayload2);
        multicast(samplePayload3);
        multicast(samplePayload4);
        multicast(softwareFailurePayload);
        multicast(samplePayload5);
        multicast(samplePayload6);
        multicast(samplePayload7);
    }

    /**
     * note: stolen & modified from the sequencer just for my personal testing purposes
     */
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
