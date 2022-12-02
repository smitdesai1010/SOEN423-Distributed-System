import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import util.DTRS;
import util.IDTRS.EventType;

public class Server {
    // TODO full logging server side
    final static int MONTREAL_REPLICA_PORT = 2301;
    final static int TORONTO_REPLICA_PORT = 2302;
    final static int VANCOUVER_REPLICA_PORT = 2303;

    enum jsonFieldNames {
        MethodName,
        adminID,
        participantID,
        eventId,
        eventType,
        capacity,
        new_eventId,
        new_eventType,

        Success,

        Data;

        String key;

        jsonFieldNames() {
            this.key = this.name();
        }
    }

    public static void main(String[] args) {
        // args = new String[] { "Montreal" };
        // if (args.length != 1) {
        // System.out.println("Incorrect argument provided");
        // return;
        // }
        DTRS dtrs;
        try {
            dtrs = new DTRS(args[0]);
        }catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
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

        System.out.println("Starting server: " + args[0]);

        while (true) {
            try {
                DatagramPacket clientRequestPacket = new DatagramPacket(new byte[1000], 1000);
                socket.receive(clientRequestPacket);
                System.out.println("received client packet");
                String clientRequestString = new String(clientRequestPacket.getData(), 0,
                        clientRequestPacket.getLength());
                JSONParser jsonParser = new JSONParser();
                JSONObject clientRequestObject = (JSONObject) jsonParser.parse(clientRequestString);

                JSONObject response = null;

                String eventId, participantId;
                EventType eventType;

                switch ((String) clientRequestObject.get(jsonFieldNames.MethodName.key)) {
                    case "die":
                        System.out.println("DYING");
                        return;
                    case "addReservationSlot":
                        eventId = (String) clientRequestObject.get(jsonFieldNames.eventId.key);
                        eventType = EventType
                                .valueOf(((String) clientRequestObject.get(jsonFieldNames.eventType.key)));
                        int capacity = Math.toIntExact((Long) clientRequestObject.get(jsonFieldNames.capacity.key));
                        response = dtrs.addReservationSlot(eventId, eventType, capacity);
                        break;
                    case "removeReservationSlot":
                        eventId = (String) clientRequestObject.get(jsonFieldNames.eventId.key);
                        eventType = EventType
                                .valueOf((String) clientRequestObject.get(jsonFieldNames.eventType.key));
                        response = dtrs.removeReservationSlot(eventId, eventType);
                        break;
                    case "listReservationSlotAvailable":
                        eventType = EventType
                                .valueOf((String) clientRequestObject.get(jsonFieldNames.eventType.key));
                        String adminId = (String) clientRequestObject.get(jsonFieldNames.adminID.key);
                        response = dtrs.listReservationSlotsAvailable(adminId, eventType);
                        break;
                    case "reserveTicket":
                        participantId = (String) clientRequestObject.get(jsonFieldNames.participantID.key);
                        eventId = (String) clientRequestObject.get(jsonFieldNames.eventId.key);
                        eventType = EventType
                                .valueOf((String) clientRequestObject.get(jsonFieldNames.eventType.key));
                        response = dtrs.reserveTicket(participantId, eventId, eventType);
                        break;
                    case "getEventSchedule":
                        participantId = (String) clientRequestObject.get(jsonFieldNames.participantID.key);
                        response = dtrs.getEventSchedule(participantId);
                        break;
                    case "cancelTicket":
                        participantId = (String) clientRequestObject.get(jsonFieldNames.participantID.key);
                        eventId = (String) clientRequestObject.get(jsonFieldNames.eventId.key);
                        eventType = EventType
                                .valueOf((String) clientRequestObject.get(jsonFieldNames.eventType.key));
                        response = dtrs.cancelTicket(participantId, eventId, eventType);
                        break;
                    case "exchangeTicket": {
                        participantId = (String) clientRequestObject.get(jsonFieldNames.participantID.key);
                        eventId = (String) clientRequestObject.get(jsonFieldNames.eventId.key);
                        eventType = EventType.valueOf((String) clientRequestObject.get(jsonFieldNames.eventType.key));
                        String new_eventId = (String) clientRequestObject.get(jsonFieldNames.new_eventId.key);
                        EventType new_eventType = EventType
                                .valueOf((String) clientRequestObject.get(jsonFieldNames.new_eventType.key));
                        response = dtrs.exchangeTicket(participantId, eventId, eventType, new_eventId, new_eventType);
                        break;
                    }
                    default:
                        break;
                }

                if (response == null) {
                    response = new JSONObject();
                    response.put(jsonFieldNames.Success.key, false);
                    response.put(jsonFieldNames.Data.key, "Error obtaining response from the server.");
                }

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
