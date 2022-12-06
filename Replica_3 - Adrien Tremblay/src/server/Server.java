package server;

import general.City;
import general.EventType;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static server.UdpServerThread.eventMapToReservationSlotAvailableCount;
import static server.UdpServerThread.eventMapToString;

public class Server {
    private static final String USAGE_MESSAGE = "Usage: server [MONTREAL/TORONTO/VANCOUVER] [port]";
    final static int MONTREAL_REPLICA_PORT = 2301;
    final static int TORONTO_REPLICA_PORT = 2302;
    final static int VANCOUVER_REPLICA_PORT = 2303;

    public static void main(String args[]) {
        Logger serverLogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        try {
            FileHandler fh = new FileHandler("server.log");
            serverLogger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (args.length != 1) {
            serverLogger.severe("Please provide a city name");
            serverLogger.severe(USAGE_MESSAGE);
            return;
        }

        City city;
        int port;
        switch(args[0]) {
            case "Montreal":
                city = City.MONTREAL;
                port = MONTREAL_REPLICA_PORT;
                break;
            case "Toronto":
                city = City.TORONTO;
                port = TORONTO_REPLICA_PORT;
                break;
            case "Vancouver":
                city = City.VANCOUVER;
                port = VANCOUVER_REPLICA_PORT;
                break;
            default:
                serverLogger.severe("Not a valid city name!");
                serverLogger.severe(USAGE_MESSAGE);
                return;
        }

        CityReservationSystem cityReservationSystem;
        DatagramSocket udpSocket;
        try {
            cityReservationSystem = new CityReservationSystem(city);
            udpSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            serverLogger.severe("socket error");
            return;
        }

        while (true) {
            try {
                // Create a packet for the client request
                DatagramPacket clientRequestPacket = new DatagramPacket(new byte[1000], 1000);
                // Receive a request using the packet we just created
                udpSocket.receive(clientRequestPacket);

                String clientRequestString = new String(clientRequestPacket.getData(), 0, clientRequestPacket.getLength());
                JSONParser jsonParser = new JSONParser();
                JSONObject clientRequestObject = (JSONObject) jsonParser.parse(clientRequestString);

                // todo remove repeated code by extracting stuff to methods
                boolean succ = true;
                String replyString = "";
                boolean die = false;
                switch ((String) clientRequestObject.get(jsonFieldNames.METHOD_NAME)) {
                    case "die" : {
                        die = true;
                        succ = true;
                        break;
                    }
                    case "addReservationSlot": {
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        int capacity = Math.toIntExact((Long) clientRequestObject.get(jsonFieldNames.CAPACTIY));
                        succ = cityReservationSystem.addReservationSlot(eventId, eventType, capacity);
                        break;
                    }
                    case "removeReservationSlot": {
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        succ = cityReservationSystem.removeReservationSlot(eventId, eventType);
                        break;
                    }
                    case "listReservationSlotAvailable": {
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        replyString = cityReservationSystem.listReservationSlotsAvailable(eventType);
                        break;
                    }
                    case "reserveTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        succ = cityReservationSystem.reserveTicket(participantId, eventId, eventType);
                        break;
                    }
                    case "getEventSchedule": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        replyString = cityReservationSystem.getEventSchedule(participantId);
                        break;
                    }
                    case "cancelTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        succ = cityReservationSystem.cancelTicket(participantId, eventId, eventType);
                        break;
                    }
                    case "exchangeTicket": {
                        String participantId = (String) clientRequestObject.get(jsonFieldNames.PARTICIPANT_ID);
                        String eventId = (String) clientRequestObject.get(jsonFieldNames.EVENT_ID);
                        EventType eventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.EVENT_TYPE));
                        String NewEventId = (String) clientRequestObject.get(jsonFieldNames.NEW_EVENT_ID);
                        EventType NewEventType = jsonStringEventTypeToEnum((String) clientRequestObject.get(jsonFieldNames.NEW_EVENT_TYPE));
                        succ = cityReservationSystem.exchangeTicket(participantId, eventId, eventType, NewEventId, NewEventType);
                        break;
                    }
                    default:
                        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.SEVERE, "Unclear what the client is asking for!");
                }

                // creating reply object
                JSONObject replyObject = new JSONObject();
                replyObject.put(jsonFieldNames.SUCCESS, succ);
                replyObject.put(jsonFieldNames.DATA, replyString);
                final byte[] replyObjectData = replyObject.toJSONString().getBytes();

                // Create a packet for the reply
                DatagramPacket serverReplyPacket = new DatagramPacket(replyObjectData, replyObjectData.length, clientRequestPacket.getAddress(), clientRequestPacket.getPort());
                // Send the reply
                udpSocket.send(serverReplyPacket);

                if (die)
                    break;
            } catch (Exception e) {
                e.printStackTrace();
                serverLogger.severe(e.getStackTrace().toString());
            }
        }
    }

    public static EventType jsonStringEventTypeToEnum(String jsonStringEventType) {
        EventType eventType = null;
        switch (jsonStringEventType) {
            case "ArtGallery":
                eventType = EventType.ART_GALLERY;
                break;
            case "Concerts":
                eventType = EventType.CONCERT;
                break;
            case "Theatre":
                eventType = EventType.THEATRE;
                break;
            default:
                Logger.getLogger(Logger.GLOBAL_LOGGER_NAME, "Failure with conversion from json EventType to the ENUM!");
        }
        return eventType;
    }
}
